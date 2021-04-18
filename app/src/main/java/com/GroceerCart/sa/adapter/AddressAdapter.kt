package com.GroceerCart.sa.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.service.address.Table
import kotlinx.android.synthetic.main.address_item_list.view.*

class AddressAdapter(
    activity: FragmentActivity?,
    table: List<Table>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val addressList : List<Table> = table
    private val context : FragmentActivity? = activity
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.address_item_list, parent, false)
        sharedPreferences = context?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()

        viewHolder.itemView.tvCustomerName.text = addressList.get(position).customeraddress_firstname + " " + addressList.get(position).customeraddress_lastname
        viewHolder.itemView.tvCustomerArea.text = addressList.get(position).customeraddress_address
        viewHolder.itemView.tvCustomerCity.text = addressList.get(position).customeraddress_city
        viewHolder.itemView.tvCustomerPinState.text = addressList.get(position).customeraddress_state + " " + addressList.get(position).customeraddress_pinCode
        viewHolder.itemView.ivAddressSelect.visibility = View.GONE
        context?.resources?.getColor(R.color.white)?.let { it1 ->
            viewHolder.itemView.addressLayout.setBackgroundColor(it1)
        }
        var addressId:Int = sharedPreferences.getInt("addressId",0)
        if(addressList.get(position).customeraddress_id.equals(addressId)){
            context?.resources?.getColor(R.color.select_colot)?.let { it1 ->
                viewHolder.itemView.addressLayout.setBackgroundColor(it1)
            }
            viewHolder.itemView.ivAddressSelect.visibility = View.VISIBLE
        }

        viewHolder.itemView.addressLayout.setOnClickListener {

            editor.putInt("addressId",addressList.get(position).customeraddress_id)
            editor.commit()
            editor.apply()
            context?.resources?.getColor(R.color.select_colot)?.let { it1 ->
                viewHolder.itemView.addressLayout.setBackgroundColor(it1)
            }
            viewHolder.itemView.ivAddressSelect.visibility = View.VISIBLE
            notifyDataSetChanged()
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCustomerName: TextView
        var tvCustomerArea: TextView
        var tvCustomerCity: TextView
        var tvCustomerPinCode: TextView
        var addressLayout : ConstraintLayout
        var ivSelectItem : ImageView
        init {
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName)
            tvCustomerArea = itemView.findViewById(R.id.tvCustomerArea)
            tvCustomerCity = itemView.findViewById(R.id.tvCustomerCity)
            tvCustomerPinCode = itemView.findViewById(R.id.tvCustomerPinState)
            addressLayout = itemView.findViewById(R.id.addressLayout)
            ivSelectItem = itemView.findViewById(R.id.ivAddressSelect)
        }
    }
}