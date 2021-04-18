package com.GroceerCart.sa.adapter

import android.content.Context

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup

import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R


class SimpleAdapter(context: Context) :
    RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder>() {
    private val mContext: Context
    private val mItems: MutableList<Int>
    private var mCurrentItemId = 0

    class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView

        init {
            title = view.findViewById(R.id.title)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SimpleViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.item, parent, false)
        return SimpleViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SimpleViewHolder,
        position: Int
    ) {
        holder.title.text = mItems[position].toString()
    }

    fun addItem(position: Int) {
        val id = mCurrentItemId++
        mItems.add(position, id)
        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        mItems.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    companion object {
        private const val COUNT = 100
    }

    init {
        mContext = context
        mItems = ArrayList(COUNT)
        for (i in 0 until COUNT) {
            addItem(i)
        }
    }
}