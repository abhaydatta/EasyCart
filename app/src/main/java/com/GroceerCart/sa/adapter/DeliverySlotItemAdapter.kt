package com.GroceerCart.sa.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.db.vendor.Vendor
import com.GroceerCart.sa.service.deliveryslot.Table
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.delivery_slot_item.view.*


class DeliverySlotItemAdapter(
    context: Context,
    slotList: List<Table>,
    vendor: Vendor,
    mlister: onClickItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var mContext : Context = context
    private var deliverySlotList:List<Table> = slotList
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"
    private var listener:onClickItem = mlister
    private var vendor: Vendor = vendor
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.delivery_slot_item, parent, false)
        sharedPreferences = mContext?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return deliverySlotList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        viewHolder.itemView.tvDeliverySlotName.setText(deliverySlotList.get(position).deliveryslots_name)
        viewHolder.itemView.tvDeliverySlotTime.setText(deliverySlotList.get(position).deliveryslots_fromTime
                + "-" + deliverySlotList.get(position).deliveryslots_toTime)
        var gson = Gson()
        var slotIdString: String? = sharedPreferences.getString("deliveryslots_id","")
        if (slotIdString != null) {
            if (slotIdString.isNotEmpty()){
                val type =
                    object : TypeToken<HashMap<Int?, Int?>?>() {}.type
                val slotListMap: HashMap<Int, Int> =
                    gson.fromJson(slotIdString, type)

                var selecetdSlotIds: Int ? = slotListMap[vendor.seqno]
                if (selecetdSlotIds == deliverySlotList.get(position).deliveryslots_id){
                    viewHolder.itemView.ivSlotSelect.visibility = View.VISIBLE
                }else{
                    viewHolder.itemView.ivSlotSelect.visibility = View.GONE
                }
            }
        }
        
      /*  if(deliverySlotList.get(position).deliveryslots_id.equals(slotId)){
            viewHolder.itemView.ivSlotSelect.visibility = View.VISIBLE
        }else{
            viewHolder.itemView.ivSlotSelect.visibility = View.GONE
        }*/
        viewHolder.itemView.slot_layout.setOnClickListener {
           /* editor.putInt("deliveryslots_id",deliverySlotList.get(position).deliveryslots_id)
            editor.commit()
            editor.apply()*/
            viewHolder.itemView.ivSlotSelect.visibility = View.VISIBLE
            notifyDataSetChanged()
            listener.onItemClick(vendor.seqno,deliverySlotList.get(position).deliveryslots_id)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var slotTitle: TextView
        var slotTime: TextView
        var slotSelectView: ImageView
        var slotLayout : ConstraintLayout

        init {
            slotTitle = itemView.findViewById(R.id.tvDeliverySlotName)
            slotTime = itemView.findViewById(R.id.tvDeliverySlotTime)
            slotSelectView = itemView.findViewById(R.id.ivSlotSelect)
            slotLayout = itemView.findViewById(R.id.deliverySlotLayout)

        }
    }

    interface onClickItem{
        fun onItemClick(vendorId: Int, deliveryslotsId: Int)
    }
}