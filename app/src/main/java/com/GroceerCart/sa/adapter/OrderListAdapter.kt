package com.GroceerCart.sa.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerOrderDetailListActivity
import com.GroceerCart.sa.activity.GroceerOrderListActivity
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.cart.ItemDAO
import com.GroceerCart.sa.db.cart.ItemRepository
import com.GroceerCart.sa.db.db.cart.Item
import com.GroceerCart.sa.db.tax.Tax
import com.GroceerCart.sa.db.tax.TaxDAO
import com.GroceerCart.sa.db.tax.TaxRepository
import com.GroceerCart.sa.db.taxitem.TaxItem
import com.GroceerCart.sa.db.taxitem.TaxItemDAO
import com.GroceerCart.sa.db.taxitem.TaxItemRepository
import com.GroceerCart.sa.db.vendor.Vendor
import com.GroceerCart.sa.db.vendor.VendorDAO
import com.GroceerCart.sa.db.vendor.VendorRepository
import kotlinx.android.synthetic.main.activity_order_list_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class OrderListAdapter(
    context: GroceerOrderListActivity,
    vendor: MutableList<Vendor>,
    priceSymbol: String?,
    mOrderTotal: MutableList<Double>,
    mOrderSubTotal: MutableList<Double>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val activity: GroceerOrderListActivity = context
    private val vendorList:List<Vendor> = vendor
    private var viewPool = RecyclerView.RecycledViewPool()
    private var orderTotal:MutableList<Double> = mOrderTotal
    private var orderSubTotal:MutableList<Double> = mOrderSubTotal
    private var priceSymbol:String? = priceSymbol
    private lateinit var itemList : List<Item>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_order_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return vendorList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            itemList = getItemByVendorId(vendorList.get(position).seqno)
            viewHolder.itemView.tvHeaderText.setText(vendorList.get(position).clientName)
            val childLayoutManager = LinearLayoutManager(viewHolder.itemView.orderItemRecyclerView.context)
            viewHolder.itemView.orderItemRecyclerView.apply {
                layoutManager = childLayoutManager
                adapter = OrderListItemAdapter(context,itemList)
                setRecycledViewPool(viewPool)
            }

            var taxArray :MutableMap<Int,Double> = HashMap()
            var taxItemSelecetdList:MutableList<TaxItem> = ArrayList()

            var taxItemList:List<TaxItem> = getItemTaxByVendor(vendorList.get(position).seqno)

            for (taxItem in taxItemList){
                if (taxArray.containsKey(taxItem.seqno)){
                    var price = taxArray.getValue(taxItem.seqno) + taxItem.taxPrice
                    taxArray.put(taxItem.seqno, price)
                }else{
                    taxArray[taxItem.seqno] = taxItem.taxPrice
                    taxItemSelecetdList.add(taxItem)
                }
            }

            /*for (item in itemList){
                orderSubTotal += item.cartPrice
                orderTotal += item.cartPrice + item.taxAmount
            }*/
            viewHolder.itemView.tvOrderTotal.text =
                priceSymbol + String.format("%.2f", orderTotal.get(position)).toDouble().toString()
            viewHolder.itemView.tvOrderSubTotal.text =
                priceSymbol + String.format("%.2f", orderSubTotal.get(position)).toDouble().toString()


            for (tax in taxItemSelecetdList){
                var taxPrice: Double = taxArray.getValue(tax.seqno)

                val relativeLayout = RelativeLayout(activity)
                val rparams = RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)

                val lparamsLeft = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                lparamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
                val tvTaxType = TextView(activity)
                tvTaxType.setPadding(10,5,5,5)
                tvTaxType.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0F)
                tvTaxType.setTextColor(activity.resources.getColor(R.color.splash_welcome_text_color))
                tvTaxType.layoutParams = lparamsLeft
                tvTaxType.text = tax.name

                val lparamsRight = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                lparamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                val tvTaxValue = TextView(activity)
                tvTaxType.setPadding(10,5,5,5)
                tvTaxType.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0F)
                tvTaxType.setTextColor(activity.resources.getColor(R.color.splash_welcome_text_color))
                tvTaxValue.layoutParams = lparamsRight
                val bd: BigDecimal = BigDecimal(taxPrice).setScale(2, RoundingMode.HALF_UP)
                val price: Double = bd.toDouble()
                tvTaxValue.text = priceSymbol + price

                relativeLayout.layoutParams = rparams
                relativeLayout.addView(tvTaxType)
                relativeLayout.addView(tvTaxValue)

                viewHolder.itemView.layout_tax.addView(relativeLayout)
            }


        }
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var orderVendorTitle: TextView
        var orderTotal :TextView
        var orderSubTotal :TextView
        var orderItemRecyclerView: RecyclerView
        val taxLayout:LinearLayout
        init {
            orderVendorTitle = itemView.findViewById(R.id.tvHeaderText)
            orderItemRecyclerView = itemView.findViewById(R.id.orderItemRecyclerView)
            orderTotal = itemView.findViewById(R.id.tvOrderTotal)
            taxLayout = itemView.findViewById(R.id.layout_tax)
            orderSubTotal = itemView.findViewById(R.id.tvOrderSubTotal)
        }
    }

    suspend fun getAllTax(productParentId: Int): List<Tax> {
        val dao: TaxDAO = GroceerDatabase.getInstance(activity).taxDAO
        val respository = TaxRepository(dao)
        var tax:List<Tax> = respository.getTaxWithProductParentId(productParentId)
        return tax
    }

    suspend fun getAllCartItem(productId: Int):List<Item>{
        val dao: ItemDAO = GroceerDatabase.getInstance(activity).itemDAO
        val respository = ItemRepository(dao)
        var item:List<Item> = respository.getItemWithId(productId)
        return item
    }

    suspend fun getAllVendor():List<Vendor>{
        val dao: VendorDAO = GroceerDatabase.getInstance(activity).vendorDAO
        val respository = VendorRepository(dao)
        var vendorList:List<Vendor> = respository.getAllVendor()
        return vendorList
    }

    suspend fun getItemByVendorId(vendorId: Int):List<Item>{
        val dao: ItemDAO = GroceerDatabase.getInstance(activity).itemDAO
        val respository = ItemRepository(dao)
        var item:List<Item> = respository.getItemWithVendorId(vendorId)
        return item
    }

    suspend fun getItemTaxByVendor(vendorId: Int):List<TaxItem>{
        val dao: TaxItemDAO = GroceerDatabase.getInstance(activity).taxItemDAO
        val respository = TaxItemRepository(dao)
        var item:List<TaxItem> = respository.getTaxItemWithVendorId(vendorId)
        return item
    }

}