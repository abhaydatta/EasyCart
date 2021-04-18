package com.GroceerCart.sa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.db.db.cart.Item
import kotlinx.android.synthetic.main.activity_order_sublist.view.*
import java.text.DecimalFormat

class OrderListItemAdapter(
    context: Context,
    itemList: List<Item>
):  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mContext : Context = context
    private var mItemList : List<Item> = itemList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_order_sublist, parent, false)
        return ViewHolder(v)    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.orderItemText.setText(mItemList.get(position).productName)
        holder.itemView.orderItemQty.setText(mItemList.get(position).cartItem.toString())
        val price: Double = mItemList.get(position).cartItem * String.format("%.2f", mItemList.get(position).PurchasePrice).toDouble()
        holder.itemView.orderItemPrice.setText(price.toString())

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var orderItemTitle: TextView
        var orderItemQty: TextView
        var orderItemPrice: TextView
        init {
            orderItemTitle = itemView.findViewById(R.id.orderItemText)
            orderItemQty = itemView.findViewById(R.id.orderItemQty)
            orderItemPrice = itemView.findViewById(R.id.orderItemPrice)

        }
    }
}