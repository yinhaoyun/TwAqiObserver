package com.haoyun.twaqiobserver

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var mSearchView: SearchView
    private lateinit var mSearchStatus: TextView
    private lateinit var mSep: View
    private lateinit var mAdapterHorizontal: AqiAdapter
    private lateinit var mAdapter: AqiAdapter
    private lateinit var mAdapterSearch: AqiAdapter
    private lateinit var mRecycleViewHorizontal: RecyclerView
    private lateinit var mRecycleView: RecyclerView
    private val TAG = "MainActivity"
    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var mSwipe: SwipeRefreshLayout
    private var mPm25Thread: Int = 30
    private lateinit var mContext: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_main)

        mRecycleViewHorizontal= findViewById(R.id.recyclerViewHorizontal)
        mRecycleViewHorizontal.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRecycleView = findViewById(R.id.recyclerView)
        mRecycleView.layoutManager = LinearLayoutManager(this)
        mSwipe = findViewById(R.id.swipe)
        mSep = findViewById(R.id.sep1)
        mSep.visibility = View.INVISIBLE
        mSearchStatus = findViewById(R.id.search_status)
        setupEmptyView() // for swipe to work
        getAqiData()
        // testPost()
    }

    private fun setupEmptyView() {
        mRecycleView.adapter = null
        mSwipe.setOnRefreshListener {
            getAqiData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val menuItem: MenuItem = menu.findItem(R.id.action_search)
        mSearchView = menuItem.actionView as SearchView
        mSearchView.queryHint = "請輸入「站名」"
        mSearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, "onQueryTextSubmit: $query")
                mAdapterSearch.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: $newText")
                mAdapterSearch.filter.filter(newText)
                return false
            }
        })
        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                Log.d(TAG, "onMenuItemActionExpand")
                mRecycleViewHorizontal.visibility = View.GONE
                mRecycleView.visibility = View.INVISIBLE
                mSep.visibility = View.INVISIBLE
                mSearchStatus.visibility = View.VISIBLE
                mRecycleView.adapter = mAdapterSearch
                mSwipe.isEnabled = false
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                Log.d(TAG, "onMenuItemActionCollapse")
                mRecycleViewHorizontal.visibility = View.VISIBLE
                mRecycleView.visibility = View.VISIBLE
                mSep.visibility = View.VISIBLE
                mSearchStatus.visibility = View.INVISIBLE
                mRecycleView.adapter = mAdapter
                mSwipe.isEnabled = true
                return true
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
                mPm25Thread = calculateMedianPm25(aqiData.records)
                mAdapterHorizontal = AqiAdapter(
                    aqiData.records.filter { it.pm2_5!!.isNotEmpty() && it.pm2_5?.toInt()!! <= mPm25Thread }.sortedBy { it.pm2_5!!.toInt() },
                    R.layout.recycle_item_horizontal)
                mAdapter = AqiAdapter(aqiData.records.filter { it.pm2_5!!.isNotEmpty() && it.pm2_5!!.toInt() > mPm25Thread }.sortedByDescending { it.pm2_5!!.toInt() })
                mAdapterSearch = AqiAdapter(aqiData.records)
                mAdapterSearch.registerAdapterDataObserver(object : AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()
                        Log.d(TAG, "onQueryTextChange: mAdapterSearch.itemCount = ${mAdapterSearch.itemCount}")
                        if (mSearchView.query.isEmpty()) {
                            mSearchStatus.text = "輸入「站名」\n查詢該地區空汙資訊"
                        } else if (mAdapterSearch.itemCount == 0) {
                            mSearchStatus.text = "找不到「${mSearchView.query}」\n相關的空汙資訊"
                        } else {
                            mSearchStatus.text = ""
                        }
                        if (mSearchStatus.text.isEmpty()) {
                            mRecycleView.visibility = View.VISIBLE
                            mSearchStatus.visibility = View.INVISIBLE
                        } else {
                            mRecycleView.visibility = View.INVISIBLE
                            mSearchStatus.visibility = View.VISIBLE
                        }
                    }
                })
                mHandler.post {
                    Log.d(TAG, "Data size = ${aqiData.records.size}, PM2.5 median = $mPm25Thread")
                    Toast.makeText(mContext, "Data size = ${aqiData.records.size}, PM2.5 median = $mPm25Thread", Toast.LENGTH_LONG).show()
                    mRecycleViewHorizontal.setAdapter(mAdapterHorizontal)
                    mRecycleView.setAdapter(mAdapter)
                    mSwipe.isRefreshing = false
                    mSep.visibility = View.VISIBLE
                }
            }
        })
        Log.d(TAG, "getAqiData END")
    }

    private fun calculateMedianPm25(records: ArrayList<Records>): Int {
        val list: MutableList<Int> = mutableListOf()
        records.forEach{
            if (it.pm2_5 != null && it.pm2_5!!.isNotEmpty())
                list += it.pm2_5!!.toInt()
        }

        list.sort()
        return list[list.size / 2]
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