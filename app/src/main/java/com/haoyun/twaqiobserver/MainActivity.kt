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
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var mRecycleView: RecyclerView
    private val TAG = "MainActivity"
    private lateinit var mData: List<AqiData>
    private val mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRecycleView = findViewById(R.id.recyclerView)
        mRecycleView.layoutManager = LinearLayoutManager(this)
        getAqiData()
        // testPost()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        var menuItem: MenuItem = menu.findItem(R.id.action_search)
        var searchView: SearchView = menuItem.actionView as SearchView

        return super.onCreateOptionsMenu(menu)
    }

    private fun testPost() {
        Log.d(TAG, "testPost")
        val testUrl =
            "https://reqres.in/api/users"
        val okHttpClient = OkHttpClient().newBuilder().addInterceptor(LoggingInterceptor()).build()
        val formBody: FormBody = FormBody.Builder()
            .add("name", "HKT")
            .add("job", "Teacher")
            .build()
        val request: Request = Request.Builder().url(testUrl).post(formBody).build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("QQQ", "response: ${response.body?.string()}")
            }
        })
        Log.d(TAG, "testPost END")
    }
    private fun getAqiData() {
        Log.d(TAG, "getAqiData")
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
                mData = ArrayList()
                mData = mData + AqiData(1, "Taipei", "Taipei City", 35, "Normal")
                mData = mData + AqiData(2, "Taipei", "Taipei City", 45, "Normal")
                mData = mData + AqiData(3, "Taipei", "Taipei City", 55, "Normal")
                val adapter = AqiAdapter(mData)
                mHandler.post({ mRecycleView.setAdapter(adapter) })
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