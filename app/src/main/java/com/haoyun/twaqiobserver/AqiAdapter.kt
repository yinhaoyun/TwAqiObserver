package com.haoyun.twaqiobserver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AqiAdapter constructor(data: List<AqiData>) : RecyclerView.Adapter<AqiAdapter.mViewHolder>() {
    val mData = data
    inner class mViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val siteId: TextView = itemView.findViewById(R.id.site_id)
        val siteName: TextView = itemView.findViewById(R.id.site_name)
        val county: TextView = itemView.findViewById(R.id.county)
        val pm2_5_avg: TextView = itemView.findViewById(R.id.pm2_5_avg)
        val status: TextView = itemView.findViewById(R.id.status)
        fun bind(data: AqiData) {
            siteId.text = data.siteId.toString()
            siteName.text = data.siteName
            county.text = data.county
            pm2_5_avg.text = data.pm2_5.toString()
            status.text = data.status
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): mViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.recycle_item, parent, false)
        return mViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: mViewHolder, position: Int) {
        holder.bind(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.size
    }

}
