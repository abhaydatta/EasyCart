package com.GroceerCart.sa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerDeliverySlotActivity
import com.GroceerCart.sa.db.vendor.Vendor
import com.GroceerCart.sa.service.deliveryslot.Table
import kotlinx.android.synthetic.main.delivery_slot_header.view.*

class DeliverySlotTitleAdapter(
    activity: GroceerDeliverySlotActivity,
    vendorList: MutableList<Vendor>,
    slotList: List<Table>,
    lister:DeliverySlotItemAdapter.onClickItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private  var context:GroceerDeliverySlotActivity = activity
    private var cartVendorList:MutableList<Vendor> = vendorList
    private var mDeliverySlotList:List<Table> = slotList
    private var viewPool = RecyclerView.RecycledViewPool()
    var  mlister:DeliverySlotItemAdapter.onClickItem = lister
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.delivery_slot_header, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return cartVendorList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.tvVendorHeader.setText(cartVendorList.get(position).clientName)
        val childLayoutManager = LinearLayoutManager(viewHolder.itemView.recyclerDeliverySlotItem.context)
        var deliverySlotList:MutableList<Table> = ArrayList()
        for (slot in mDeliverySlotList){
            if (slot.clientid.equals(cartVendorList.get(position).seqno)){
                deliverySlotList.add(slot)
            }
        }

        viewHolder.itemView.recyclerDeliverySlotItem.apply {
            layoutManager = childLayoutManager
            adapter = DeliverySlotItemAdapter(context,deliverySlotList,cartVendorList.get(position),mlister)
            setRecycledViewPool(viewPool)
        }

    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var slotVendorTitle: TextView
        var slotRecyclerView: RecyclerView
        init {
            slotVendorTitle = itemView.findViewById(R.id.tvVendorHeader)
            slotRecyclerView = itemView.findViewById(R.id.recyclerDeliverySlotItem)
        }
    }
}