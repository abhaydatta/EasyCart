package com.GroceerCart.sa.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.OrderListDetailAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.orderlist.OrderListDetail
import com.GroceerCart.sa.service.orderlist.Table
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Response

class GroceerOrderDetailListActivity : GroceerBaseActivity() {
    private lateinit var groceerApiService: GroceerApiService
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences
    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private var layoutManager: RecyclerView.LayoutManager? = null
    private  var vendorId:Int = 0
    private lateinit var orderListRecyclerView: RecyclerView
    private lateinit var orderListResponse:List<Table>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_order_detail_list)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        var userId = intent.getIntExtra("customerId",0)

        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendorCode : String  = vendorDetail.getString("clientCode")
        vendorId   = vendorDetail.getInt("seqno")
        var location_id = vendorDetail.getString("locationId")
        var branch_id = vendorDetail.getString("branchId")
        var pinCode : String? = sharedPreferences.getString("pinCode","")

        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendorCode).create(GroceerApiService::class.java)
        layoutManager = LinearLayoutManager(this)
        orderListRecyclerView = findViewById(R.id.orderListDetailRecyclerView)
        orderListRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        orderListRecyclerView.setHasFixedSize(true)
        orderListRecyclerView.layoutManager = layoutManager

        if (getConnectionStatus() ){
            getOrderListDetail(userId)
        }else{
            // progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }

    }

    fun getOrderListDetail(userId: Int) {
        var responseLiveData : LiveData<Response<OrderListDetail>> = liveData {
            val rootObject= JSONObject()
            rootObject.put("type",3)
            rootObject.put("userId",userId)
            rootObject.put("filterId",0)
            rootObject.put("clientId","")
            rootObject.put("condition","")
            rootObject.put("clientSeqno",0)
            rootObject.put("searchName","")
            rootObject.put("locationId",0)
            rootObject.put("branchId",0)

            Log.e("Request",rootObject.toString())

            val response = groceerApiService.getOrderDetailsList(rootObject.toString())
            Log.e("Response",response.toString())
            emit(response)
        }
        responseLiveData.observe(this, Observer {
            val orderDetailResponseData =it?.body()
            if (orderDetailResponseData!= null){
                if (orderDetailResponseData.status.equals("200")){
                    orderListResponse = orderDetailResponseData.objresult.table
                    initAdapter(orderListResponse)

                }else{
                    //  progressBar.visibility = View.GONE
                    Toast.makeText(this,orderDetailResponseData.message,Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this,"No Records Found !",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun initAdapter(orderListResponse: List<Table>) {
        adapter = OrderListDetailAdapter(this,orderListResponse)
        orderListRecyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}