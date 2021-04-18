package com.GroceerCart.sa.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import kotlinx.android.synthetic.main.account_list_item.view.*


class AccountAdapter(
    activity: FragmentActivity?,
    accountItems: Array<String>, listener: onItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    var accountItemList :Array<String> = accountItems
    var mListener : onItemClickListener = listener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.account_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        Log.e("Array Size", accountItemList.size.toString())
        return accountItemList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.textAccountItem.text = accountItemList[position]

        viewHolder.itemView.textAccountItem.setOnClickListener {
            mListener.onItemClick(position)
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemTitle: TextView
        init {
            itemTitle = itemView.findViewById(R.id.textAccountItem)
        }
    }

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
}