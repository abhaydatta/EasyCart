package com.GroceerCart.sa.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GorceerCategoryActivity
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.service.homeservice.Table8
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.top_brand_item.view.*

class TopBrandAdapter(private val context: GroceerHomeActivity, brandList: List<Table8>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private  var brandList: List<Table8> = brandList
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.top_brand_item, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return brandList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
       /* viewHolder.itemView.tvProductTitle.text = productList.get(position).name
        viewHolder.itemView.tvProductActualPrice.text = productList.get(position).sellingPrice.toString()
        viewHolder.itemView.tvProductActualPrice.text = "$" + productList.get(position).sellingPrice.toString()*/

        viewHolder.itemView.topBrandcardView.setOnClickListener {
            context.startActivity(Intent(context, GorceerCategoryActivity::class.java))
        }

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(context).load( /*GroceerApiInstance.BASE_URL1 +*/ brandList.get(position).imagePath)
            .apply(requestOptions).into(viewHolder.itemView.imgTopBrandItem)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var brandImage: ImageView
        var cardViewBrand : CardView
       /* var productItemTitle: TextView
        var productOfferPrice: TextView
        var productActualPrice: TextView
        var btnOfferText : Button
        var btnOfferDiscount : Button
        var btnAddToCart : Button*/

        init {
            brandImage = itemView.findViewById(R.id.imgTopBrandItem)
            cardViewBrand = itemView.findViewById(R.id.topBrandcardView)
            /*productItemTitle = itemView.findViewById(R.id.tvProductTitle)
            productOfferPrice = itemView.findViewById(R.id.tvProductOfferPrice)
            productActualPrice = itemView.findViewById(R.id.tvProductActualPrice)
            btnOfferText = itemView.findViewById(R.id.btnOfferText)
            btnOfferDiscount = itemView.findViewById(R.id.btnItemDiscount)
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart)*/

        }
    }
}