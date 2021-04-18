package com.GroceerCart.sa.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.GroceerCart.sa.R
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.productitem.ProductItem
import com.GroceerCart.sa.service.productitem.Table
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_groceer_all_product.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

class GroceerProductListActivity : GroceerBaseActivity() {
    private lateinit var groceerApiService: GroceerApiService
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_product_list)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        var category_id = intent.getIntExtra("categoryId",0)
        var subCateogry_id = intent.getIntExtra("subCategoryId",0)

        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendor_code : String  = vendorDetail.getString("clientCode")
        var vendor_id :String  = vendorDetail.getString("seqno")
        var location_id = vendorDetail.getString("locationId")
        var branch_id = vendorDetail.getString("branchId")

        var pinCode : String? = sharedPreferences.getString("pinCode","")

        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(GroceerApiService::class.java)

        if (getConnectionStatus() ){
            getAllProductList(vendor_id,category_id,pinCode)
        }else{
            //progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }

    }

    private fun getAllProductList(
        vendorId: String,
        categoryId: Int,
        pinCode: String?
    ) {
        var responseLiveData : LiveData<Response<ProductItem>> = liveData {

            val searchArray = JSONArray()
            val searchObject = JSONObject()
            searchObject.put("typeId",1)
            searchObject.put("keyword",categoryId)
            searchArray.put(searchObject)

            val rootObject= JSONObject()
            rootObject.put("vendorId",vendorId)
            rootObject.put("pinCode",pinCode)
            rootObject.put("searchList",searchArray)
            rootObject.put("pageNo",1)
            rootObject.put("pageCount",50)


            Log.e("String",rootObject.toString())

            val response = groceerApiService.getProductList(rootObject.toString())
            emit(response)
        }

        responseLiveData.observe(this, Observer {
            val productResponseData =it?.body()
            if (productResponseData!= null){
                if (productResponseData.status.equals("200")){
                    val produuctListResponse: List<Table> = productResponseData.objresult.table
                  //  initAdapter(produuctListResponse)
                }else{
                    //  progressBar.visibility = View.GONE
                    Toast.makeText(this,productResponseData.message.toString(), Toast.LENGTH_LONG).show()
                }

            }
        })
    }

   /* private fun initAdapter(produuctListResponse: List<Table>) {
        productListAdapter = this?.let { CategoryProductAdapter(it,produuctListResponse) }!!
        itemListGridView.adapter = productListAdapter
    }*/

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}