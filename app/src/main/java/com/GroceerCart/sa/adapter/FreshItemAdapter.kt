package com.GroceerCart.sa.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GorceerCategoryActivity
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.service.homeservice.Table2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.home_layout_top_two.view.*

class FreshItemAdapter(private val context: GroceerHomeActivity, freshList: List<Table2>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private  var freshList: List<Table2> = freshList
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.home_layout_top_two, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return freshList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.textTitle.text = freshList.get(position).title
        viewHolder.itemView.textSubTitle.text = freshList.get(position).title1
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(context).load(/*GroceerApiInstance.BASE_URL1 +*/ freshList.get(position).imagePath)
            .apply(requestOptions).into(viewHolder.itemView.imgFresh)
        viewHolder.itemView.cardViewFreshItem.setOnClickListener{
           // val activity  = context as Activity
            context.startActivity(Intent(context,GorceerCategoryActivity::class.java))
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var itemImage: ImageView
        var itemTitle: TextView
        var itemDetail: TextView
        var cardsView : CardView

        init {
            itemImage = itemView.findViewById(R.id.imgFresh)
            itemTitle = itemView.findViewById(R.id.textTitle)
            itemDetail = itemView.findViewById(R.id.textSubTitle)
            cardsView = itemView.findViewById(R.id.cardViewFreshItem)
        }
    }
}