package com.GroceerCart.sa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.service.categoryservice.Objresult
import kotlinx.android.synthetic.main.fiter_category_list_item.view.*

class FilterCategoryAdapter(
    mContext: Context,
    listCategory: List<Objresult>,
    listener: onCheckPosition
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var filterCategoryList = arrayOf("FoodGrains","Bakery","Beverages","Snacks","Kitchen","Beauty")
    var mListener: onCheckPosition = listener
    var filterList : List<Objresult> = listCategory
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fiter_category_list_item, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.chkFilterCategory.text = filterList.get(position).name

        viewHolder.itemView.chkFilterCategory.setOnCheckedChangeListener { compoundButton, isChecked  ->
            if (isChecked){
                mListener.onCheckCategory(filterList.get(position).seqno)
            }
        }
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var filterCheckBox: CheckBox
      //  var filterCategoryTitle: TextView
        init {
            filterCheckBox = itemView.findViewById(R.id.chkFilterCategory)
         //   filterCategoryTitle = itemView.findViewById(R.id.txtFilterCategory)
        }
    }

    interface onCheckPosition{
        fun onCheckCategory(ratingId: Int)
    }
}