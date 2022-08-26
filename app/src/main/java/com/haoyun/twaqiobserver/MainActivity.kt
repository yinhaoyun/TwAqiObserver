package com.haoyun.twaqiobserver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var mAdapter: AqiAdapter
    private lateinit var mRecycleViewHorizontal: RecyclerView
    private lateinit var mRecycleView: RecyclerView
    private val TAG = "MainActivity"
    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var mSwipe: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRecycleViewHorizontal= findViewById(R.id.recyclerViewHorizontal)
        mRecycleViewHorizontal.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRecycleView = findViewById(R.id.recyclerView)
        mRecycleView.layoutManager = LinearLayoutManager(this)
        mSwipe = findViewById(R.id.swipe)
        setupEmptyView() // for swipe to work
        getAqiData()
        // testPost()
    }

    private fun setupEmptyView() {
        val data: List<Records> = listOf(Records())
        mRecycleView.setAdapter(AqiAdapter(data))
        mSwipe.setOnRefreshListener {
            getAqiData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val menuItem: MenuItem = menu.findItem(R.id.action_search)
        var searchView: SearchView = menuItem.actionView as SearchView
        searchView.queryHint = "請輸入「站名」"
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mAdapter.filter.filter(newText)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun getAqiData() {
        Log.d(TAG, "getAqiData")
        mSwipe.isRefreshing = true
        val url = "https://data.epa.gov.tw/api/v2/aqx_p_432?limit=100&api_key=cebebe84-e17d-4022-a28f-81097fda5896&sort=ImportDate%20desc&format=json"
        Log.d(TAG, "getAqiData: url = $url")
        val okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(LoggingInterceptor())
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()
        val request: Request = Request.Builder().url(url).get().build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                // Log.d(TAG, "onResponse: ${response.body?.string()}")
                Log.d(TAG, "onResponse: body length = ${response.body?.contentLength()}")
                val gson = Gson()
                val aqiData = gson.fromJson(response.body?.string(), AqiData::class.java)
                mAdapter = AqiAdapter(aqiData.records)
                mHandler.post {
                    mRecycleViewHorizontal.setAdapter(mAdapter)
                    mRecycleView.setAdapter(mAdapter)
                    mSwipe.isRefreshing = false
                }
            }
        })
        Log.d(TAG, "getAqiData END")
    }

    class LoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val t1 = System.nanoTime()
            Log.d("OkHttp", "Sending request ${request.url} on ${chain.connection()} ${request.headers}")

            val response = chain.proceed(request)

            val t2 = System.nanoTime()
            Log.d("OkHttp", "Received response for ${request.url} in ${(t2 - t1) / 1000000}ms ${response.headers}")
            return response
        }

    }
}