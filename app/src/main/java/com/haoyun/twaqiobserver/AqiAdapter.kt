package com.haoyun.twaqiobserver

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class AqiAdapter constructor(data: List<Records>, itemViewID: Int = R.layout.recycle_item) : RecyclerView.Adapter<AqiAdapter.mViewHolder>(), Filterable {
    private val mData: List<Records> = data
    private var mDataFilter: List<Records> = mData.toList()
    private var mItemViewID = itemViewID
    inner class mViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val siteId: TextView = itemView.findViewById(R.id.site_id)
        val siteName: TextView = itemView.findViewById(R.id.site_name)
        val county: TextView = itemView.findViewById(R.id.county)
        val pm2_5_avg: TextView = itemView.findViewById(R.id.pm2_5_avg)
        val status: TextView = itemView.findViewById(R.id.status)
        val arrow = itemView.findViewById<ImageView>(R.id.right_arrow)

        init {
            itemView.setOnClickListener { v ->
                if (arrow != null && arrow.visibility == View.VISIBLE) {
                    Toast.makeText(v!!.context, "So sad, the air is ${status.text} and not good~", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fun bind(data: Records) {
            siteId.text =  data.siteid
            siteName.text = data.sitename
            county.text = data.county
            pm2_5_avg.text = data.pm2_5
            if (mItemViewID == R.layout.recycle_item && data.status.equals("良好")) {
                status.text = "The status is good, we want to go out to have fun"
                arrow?.visibility = View.GONE
            } else {
                status.text = data.status
                arrow?.visibility = View.VISIBLE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): mViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(mItemViewID, parent, false)
        return mViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: mViewHolder, position: Int) {
        holder.bind(mDataFilter[position])
    }

    override fun getItemCount(): Int {
        return mDataFilter.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                mDataFilter = ArrayList()
                if (constraint == null || constraint.isEmpty()) {
                } else {
                    mData.forEach {
                        if (it.sitename!!.contains(constraint)  || it.county!!.contains(constraint)) {
                            mDataFilter = mDataFilter + it
                        }
                    }
                }
                val res = FilterResults()
                res.values = mDataFilter
                res.count = mDataFilter.size
                return res
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mDataFilter = if (results?.values == null)
                    ArrayList()
                else
                    results.values as List<Records>
                notifyDataSetChanged()
            }

        }
    }
}
