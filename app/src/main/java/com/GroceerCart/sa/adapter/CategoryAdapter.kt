package com.GroceerCart.sa.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.filter.GroceerFilterProductListActivity
import com.GroceerCart.sa.service.categoryservice.Subcategory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.category_sublist_item.view.*

class CategoryAdapter(
    context: Context,
    subcategories: List<Subcategory>
) :  RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var mContext:Context = context
    private  var subcategories: List<Subcategory> = subcategories

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.category_sublist_item, viewGroup, false)
        return ViewHolder(v)    }

    override fun getItemCount(): Int {
        return subcategories.size   }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.tvCategoryItemTitle.text = subcategories.get(position).name
        viewHolder.itemView.tvCategoryItemCount.text = "Product - " + subcategories.get(position).productCount.toString()
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(mContext).load(subcategories.get(position).imagePath)
            .apply(requestOptions).into(viewHolder.itemView.imgCategoryItem)
        viewHolder.itemView.cardViewProduuct.setOnClickListener {
            var mIntent : Intent = Intent(mContext,GroceerFilterProductListActivity::class.java)
            mIntent.putExtra("categoryId",subcategories.get(position).categoryId)
            mIntent.putExtra("subCategoryId",subcategories.get(position).seqno)
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(mIntent)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var categorySubTitle: TextView
        var categorySubCount: TextView
        var categorySubImages : ImageView
        var categoryCardView : CardView

        init {
            categorySubTitle = itemView.findViewById(R.id.tvCategoryItemTitle)
            categorySubCount = itemView.findViewById(R.id.tvCategoryItemCount)
            categorySubImages = itemView.findViewById(R.id.imgCategoryItem)
            categoryCardView = itemView.findViewById(R.id.cardViewProduuct)
        }
    }

}
