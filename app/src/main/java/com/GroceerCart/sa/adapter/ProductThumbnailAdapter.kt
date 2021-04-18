package com.GroceerCart.sa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerProductDetailActivity
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.service.productDetail.Table1
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.layout_product_thumbnail.view.*

class ProductThumbnailAdapter(
    private val context: GroceerProductDetailActivity,
    productThumbnailResponse: List<Table1>, listener:onClickPosition
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var imageList: List<Table1> = productThumbnailResponse
    private var itemPosition:Int =0
     var mListener:onClickPosition = listener
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.layout_product_thumbnail, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(context)
            .load(GroceerApiInstance.BASE_URL + imageList.get(position).filepath)
            .apply(requestOptions).into(viewHolder.itemView.imageThumbnail)
        viewHolder.itemView.imageThumbnail.setOnClickListener {
            mListener.onClick(position)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var layout_thumbnail: LinearLayout

        init {
            itemImage = itemView.findViewById(R.id.imageThumbnail)
            layout_thumbnail= itemView.findViewById(R.id.layout_thumbnail)
        }
    }

    interface onClickPosition{
        fun onClick(position: Int)
    }
}