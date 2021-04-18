package com.GroceerCart.sa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.service.country.Table
import kotlinx.android.synthetic.main.country_item_list.view.*

class CountryAdapter(
    activity: FragmentActivity?,
    mCountryList: List<Table> , listener: onItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var countryList: List<Table> = mCountryList
    private var mListener:onItemClickListener = listener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.country_item_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return countryList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.tvCountryTitle.text = countryList.get(position).country_name

        viewHolder.itemView.tvCountryTitle.setOnClickListener {
            mListener.onItemClick(position)
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var countryTitle: TextView

        init {
            countryTitle = itemView.findViewById(R.id.tvCountryTitle)
        }
    }

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }
}