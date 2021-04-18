package com.GroceerCart.sa.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.AddressAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.address.UserAddress
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_groceer_address_list.*
import org.json.JSONObject
import retrofit2.Response

class GroceerAddressListActivity : GroceerBaseActivity() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"
    private lateinit var groceerApiService: GroceerApiService
    private  var userId:Int = 0
    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var  addressRecyclerView : RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_address_list)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        userId = intent.getIntExtra("customerId",0)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendor_code : String = vendorDetail.getString("clientCode")

        layoutManager = LinearLayoutManager(this)

        addressRecyclerView = findViewById<RecyclerView>(R.id.addressRecyclerView)
        addressRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        addressRecyclerView.setHasFixedSize(true)
        addressRecyclerView.layoutManager = layoutManager

        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(
            GroceerApiService::class.java)

        if (getConnectionStatus() ){
            llProgressBar.visibility = View.VISIBLE
            getCustomerAddress(vendor_code, userId)
        }else{
            // progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }

        var addAddress = findViewById<ImageView>(R.id.imgAddAddress)
        addAddress.setOnClickListener {
            startActivityForResult(Intent(this,GroceerAddUserAddressActivity::class.java).putExtra("userId",userId),401)
        }

    }

    private fun getCustomerAddress(vendorCode: String, userId: Int) {
        var responseLiveData : LiveData<Response<UserAddress>> = liveData {
            val rootObject= JSONObject()
            rootObject.put("type",67)
            rootObject.put("filterId", userId)
            rootObject.put("clientId",vendorCode)

            Log.e("Request",rootObject.toString())

            val response = groceerApiService.getCustomerAddressList(rootObject.toString())
            Log.e("Response",response.toString())
            emit(response)
        }
        responseLiveData.observe(this, Observer {
            val userAddressResponseData =it?.body()
            var editor:SharedPreferences.Editor = sharedPreferences.edit()
            llProgressBar.visibility = View.GONE
            if (userAddressResponseData != null) {
                if (userAddressResponseData.objresult.table.size > 0){
                    editor.putBoolean("addressFlag", true)
                    initAdapter(userAddressResponseData.objresult.table)
                }else{
                    editor.putBoolean("addressFlag",false)
                }
            }else{
                editor.putBoolean("addressFlag",false)
            }
            editor.commit()
            editor.apply()
        })
    }

    private fun initAdapter(table: List<com.GroceerCart.sa.service.address.Table>) {
        adapter = AddressAdapter(this,table)
        addressRecyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 401){
            if (getConnectionStatus() ){
                var vendorCode :String? = data?.getStringExtra("vendorId")
                var cId : Int? = data?.getIntExtra("cId",0)
                Log.e("onActivityResult", vendorCode + "-" + cId)
                if (vendorCode != null && cId != null) {
                    getCustomerAddress(vendorCode, cId)
                }
                llProgressBar.visibility = View.GONE
            }else{
                 llProgressBar.visibility = View.GONE
                Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}