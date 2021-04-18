package com.GroceerCart.sa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import kotlinx.android.synthetic.main.filter_rating_item.view.*
import kotlinx.android.synthetic.main.fiter_category_list_item.view.*

class FilterRatingAdapter(mContext: Context, listener: onCheckRatingId) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var filterRatingList = arrayOf("5 Stars","4 Stars","3 Stars","2 Stars","1 Stars")
    var mListener : onCheckRatingId = listener
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.filter_rating_item, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return filterRatingList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.chkFilterRating.text = filterRatingList.get(position)
        viewHolder.itemView.chkFilterRating.setOnCheckedChangeListener { compoundButton, b ->
            when(position){
                0 ->  mListener.onCheckRating(5)
                1 ->  mListener.onCheckRating(4)
                2 ->  mListener.onCheckRating(3)
                3 ->  mListener.onCheckRating(2)
                4 ->  mListener.onCheckRating(0)
                5 ->  mListener.onCheckRating(0)
            }
        }
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var filterCheckBox: CheckBox
        //  var filterCategoryTitle: TextView
        init {
            filterCheckBox = itemView.findViewById(R.id.chkFilterRating)
            //   filterCategoryTitle = itemView.findViewById(R.id.txtFilterCategory)
        }
    }
    interface onCheckRatingId{
        fun onCheckRating(ratingId: Int)
    }
}