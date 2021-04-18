package com.GroceerCart.sa.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.service.Table
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import org.json.JSONObject
import java.util.*


class VendorListAdapter(mcontext: Context, table: List<Table>) : BaseAdapter(),Filterable {
    var vendorList : List<Table> = table
    var vendorListFilter : List<Table> = vendorList
    private lateinit var  sharedPreferences: SharedPreferences
    var context:Context = mcontext
    private val sharedPrefFile = "groceerpreference"


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Inflate the custom view
        val inflater = parent?.context?.
        getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.vendor_list_item,null)

        // Get the custom view widgets reference
        val tvVendorTitle = view.findViewById<TextView>(R.id.tvName)
        val tvLastOrderDate = view.findViewById<TextView>(R.id.txtLastOrder)
        val tvTotalOrder = view.findViewById<TextView>(R.id.txtTotalOrder)
        val card = view.findViewById<CardView>(R.id.cardView)
        val imgItem = view.findViewById<ImageView>(R.id.imgFood)
        val btnSelect = view.findViewById<Button>(R.id.btnVendorSelect)
        val rating = view.findViewById<RatingBar>(R.id.ratingVendor)

        // Display color name on text view
        tvVendorTitle.text = vendorList.get(position).clientName
        tvLastOrderDate.text =  vendorList.get(position).lastOrderDate
        tvTotalOrder.text = vendorList.get(position).totalOrders.toString()
        rating.rating = vendorList.get(position).ratings.toFloat()
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(context).load( vendorList.get(position).clientImage)
            .apply(requestOptions).into(imgItem)

        // Set a click listener for card view
        btnSelect.setOnClickListener{
            // Show selected color in a toast message
       /*     Toast.makeText(parent.context,
                "Clicked : }" + vendorList.get(position).clientName,Toast.LENGTH_SHORT).show()
*/

            // Get the activity reference from parent
            val activity  = parent.context as Activity
            sharedPreferences = activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
            val editor:SharedPreferences.Editor =  sharedPreferences.edit()

            var vendorObj : JSONObject = JSONObject()
            vendorObj.put("seqno",vendorList.get(position).seqno)
            vendorObj.put("clientName",vendorList.get(position).clientName)
            vendorObj.put("clientCode",vendorList.get(position).clientCode)
            vendorObj.put("locationId",vendorList.get(position).locationId)
            vendorObj.put("branchId",vendorList.get(position).branchId)
            vendorObj.put("checkQuantity",vendorList.get(position).checkQuantity)

            var vendorObejctStrting: String? = sharedPreferences.getString("vendorObject","")
            val gson = Gson()
            var  vendorJson : String = gson.toJson(vendorObj)
            if (vendorObejctStrting?.isNotEmpty()!!){
                editor.putString("vendorObject",vendorJson)
            }else{
                editor.remove("vendorObject")
                editor.putString("vendorObject",vendorJson)
            }

            // Get the activity root view
            val viewGroup = activity.findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0)

            var vendor_id:String = vendorList.get(position).seqno.toString()
            var vendor_name:String = vendorList.get(position).clientName.toString()


            editor.putString("vendorId",vendor_id)
            editor.commit()
            editor.apply()

            activity.startActivity(Intent(activity,GroceerHomeActivity::class.java))
            activity.finish()

        }

        // Finally, return the view
        return view
    }

    override fun getItem(position: Int): Any {
        return vendorList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return vendorList.size
    }

    override fun getFilter(): Filter {
        return object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val charSearch = constraint?.trim().toString()
                if (charSearch.isEmpty()) {
                     vendorList = vendorListFilter
                } else {
                    val resultList = ArrayList<Table>()
                    for (row in vendorList) {
                        if (row.clientName.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    vendorList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = vendorList
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                vendorList = results?.values as ArrayList<Table>
                notifyDataSetChanged()
            }

        }
    }
}