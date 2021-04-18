package com.GroceerCart.sa.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.DeliverySlotItemAdapter
import com.GroceerCart.sa.adapter.DeliverySlotTitleAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.db.db.cart.Item
import com.GroceerCart.sa.db.vendor.Vendor
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.deliveryslot.DeliverySlot
import com.GroceerCart.sa.service.deliveryslot.Table
import com.GroceerCart.sa.service.homeservice.Table6
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_groceer_delivery_slot.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class GroceerDeliverySlotActivity : GroceerBaseActivity(),DeliverySlotItemAdapter.onClickItem {
    private lateinit var deliverySlotRecyclerView: RecyclerView
    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var groceerApiService: GroceerApiService
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences
    private  var vendorId:Int = 0
    lateinit var  cartVendorList : MutableList<Vendor>
    private  var vendorArray:String = ""
    private lateinit var slotList:HashMap<Int,Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.activity_groceer_delivery_slot
        )
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendorCode: String = vendorDetail.getString("clientCode")
        vendorId = vendorDetail.getInt("seqno")
        var vendor_branchId = vendorDetail.getInt("branchId")
        var vendor_locationId = vendorDetail.getInt("locationId")
        var pinCode: String? = sharedPreferences.getString("pinCode", "")
        var userId:Int = sharedPreferences.getInt("userId",0)

        groceerApiService =
            GroceerApiInstance.getRetrofitInstance(vendorCode).create(GroceerApiService::class.java)

        layoutManager = LinearLayoutManager(this)
        deliverySlotRecyclerView = findViewById(R.id.slotRecyclerView)
        deliverySlotRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        deliverySlotRecyclerView.setHasFixedSize(true)
        deliverySlotRecyclerView.layoutManager = layoutManager

        CoroutineScope(Dispatchers.Main).launch {
            var vendorList: List<Vendor> = getAllVendor()
            cartVendorList = ArrayList()
            for (vendor in vendorList) {
                var itemList: List<Item> = getItemByVendorId(vendor.seqno)
                if (itemList.size > 0) {
                    cartVendorList.add(vendor)
                }
            }
            var seq:Int = 1
            for (vendor in cartVendorList){
                if (seq.equals(cartVendorList.size)){
                    vendorArray += vendor.seqno.toString()
                }else{
                    vendorArray += vendor.seqno.toString() + ","
                }
                seq++
            }
            llProgressBar.visibility = View.VISIBLE
            getDeliverySlot(vendorArray,vendor_locationId,vendor_branchId,pinCode,userId)
        }

    }

    private fun getDeliverySlot(
        vendorArray: String,
        locationId: Int,
        branchId: Int,
        pinCode: String?,
        userId: Int
    ) {
        var responseLiveData : LiveData<Response<DeliverySlot>> = liveData {
            val rootObject= JSONObject()
            rootObject.put("type",79)
            rootObject.put("userId",userId)
            rootObject.put("filterId",pinCode)
            rootObject.put("condition","")
            rootObject.put("clientSeqno",0)
            rootObject.put("searchName","")
            rootObject.put("clientId",vendorArray)
            rootObject.put("locationId",locationId)
            rootObject.put("branchId",branchId)

            Log.e("Request",rootObject.toString())

            val response = groceerApiService.getDeliverySlots(rootObject.toString())
            emit(response)
        }

        responseLiveData.observe(this, Observer {
            val slotResponseData =it?.body()
            if (slotResponseData!= null){
                if (slotResponseData.status.equals("200")){
                    setAdapter(slotResponseData.objresult.table)
                    llProgressBar.visibility = View.GONE
                }
            }else{
                 llProgressBar.visibility = View.GONE
                Toast.makeText(this,slotResponseData?.message.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun setAdapter(slotList: List<Table>) {
        var vendorList:MutableList<Vendor> = ArrayList()
        for (vendor in cartVendorList){
            for (slot in slotList){
                if (vendor.seqno.equals(slot.clientid)){
                    vendorList.add(vendor)
                    break
                }
            }
        }

        var listener:DeliverySlotItemAdapter.onClickItem = this
        slotRecyclerView.setHasFixedSize(true)
        slotRecyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = DeliverySlotTitleAdapter(this@GroceerDeliverySlotActivity,vendorList,slotList,listener)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onItemClick(vendorId: Int, deliveryslotsId: Int) {
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        slotList = HashMap()
        var gson = Gson()
        var slotIdString: String? = sharedPreferences.getString("deliveryslots_id","")
        if (slotIdString != null) {
            if (slotIdString.isNotEmpty()) {
                val type =
                    object : TypeToken<HashMap<Int?, Int?>?>() {}.type
                slotList =
                    gson.fromJson(slotIdString, type)
            }
        }
        if (slotList.containsKey(vendorId)){
            var slotIds: Int = deliveryslotsId
            slotList[vendorId] = slotIds
        }else {
            slotList[vendorId] = deliveryslotsId
        }
        var deliverySlotString:String = gson.toJson(slotList)
        editor.putString("deliveryslots_id",deliverySlotString)
        editor.commit()
        editor.apply()
    }

}