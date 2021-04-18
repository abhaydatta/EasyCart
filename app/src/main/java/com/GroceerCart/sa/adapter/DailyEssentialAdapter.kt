package com.GroceerCart.sa.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GorceerCategoryActivity
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.activity.GroceerProductDetailActivity
import com.GroceerCart.sa.service.homeservice.Table7
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.daily_list_item.view.*

class DailyEssentialAdapter(private val context: GroceerHomeActivity, dailyList: List<Table7>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private  var dailyList: List<Table7> = dailyList
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.daily_list_item, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dailyList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.tvDailyItem.text = dailyList.get(position).name
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(context).load(/*GroceerApiInstance.BASE_URL1 +*/ dailyList.get(position).imagePath)
            .apply(requestOptions).into(viewHolder.itemView.imgDailyItem)
        viewHolder.itemView.linearDailyItemLayout.setOnClickListener {
            context.startActivity(Intent(context, GroceerProductDetailActivity::class.java).putExtra("productId",dailyList.get(position).productId))
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var itemTitle: TextView
        var layout_dailyItem : LinearLayout
        init {
            itemImage = itemView.findViewById(R.id.imgDailyItem)
            itemTitle = itemView.findViewById(R.id.tvDailyItem)
            layout_dailyItem = itemView.findViewById(R.id.linearDailyItemLayout)
        }
    }
}