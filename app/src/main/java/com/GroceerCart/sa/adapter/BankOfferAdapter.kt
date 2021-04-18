package com.GroceerCart.sa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.ui.home.HomeFragment
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.service.homeservice.Table6
import com.GroceerCart.sa.service.homeservice.Table9
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.daily_list_item.view.*
import kotlinx.android.synthetic.main.home_bank_offer.view.*

class BankOfferAdapter (private val context: HomeFragment, offersList: List<Table9>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private  var offersList: List<Table9> = offersList
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.home_bank_offer, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return offersList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.tvOffersTitle.text = offersList.get(position).title
        viewHolder.itemView.tvOfferDescription.text = offersList.get(position).shortDescription
        viewHolder.itemView.tvOfferPrice.text = "$" + offersList.get(position).offerPrice.toString()
        viewHolder.itemView.btnOfferCode.text = offersList.get(position).offerCode

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(context).load( offersList.get(position).imageUrl)
            .apply(requestOptions).into(viewHolder.itemView.imgBank)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var bankOfferImage: ImageView
        var bankOfferTitle: TextView
        var bankOfferDesc: TextView
        var bankOfferPrice: TextView
        var btnOfferCode : Button

        init {
            bankOfferImage = itemView.findViewById(R.id.imgBank)
            bankOfferTitle = itemView.findViewById(R.id.tvOffersTitle)
            bankOfferDesc = itemView.findViewById(R.id.tvOfferDescription)
            bankOfferPrice = itemView.findViewById(R.id.tvOfferPrice)
            btnOfferCode = itemView.findViewById(R.id.btnOfferCode)
        }
    }
}