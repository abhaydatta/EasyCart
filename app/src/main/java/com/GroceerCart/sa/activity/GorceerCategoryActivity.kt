package com.GroceerCart.sa.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.CategoryListAdater
import com.GroceerCart.sa.adapter.SimpleAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.filter.GroceerFilterProductListActivity
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.categoryservice.Category
import com.GroceerCart.sa.service.categoryservice.Objresult
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_gorceer_category.*
import kotlinx.android.synthetic.main.layout_top_categorylist.*
import org.json.JSONObject
import retrofit2.Response


class GorceerCategoryActivity : GroceerBaseActivity() {
    private lateinit var categoryListView: RecyclerView
    private lateinit var mRecyclerView : RecyclerView
    private lateinit var mAdapter:SimpleAdapter
    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"

    private lateinit var groceerApiService: GroceerApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gorceer_category)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json,JSONObject::class.java)

        var vendor_code : String = vendorDetail.getString("clientCode")
        var vendor_id : Int = vendorDetail.getInt("seqno")


        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(GroceerApiService::class.java)
        categoryListView = findViewById<RecyclerView>(R.id.categoryListView)


       // var vendor_id: String? = sharedPreferences.getString("vendorId","")
        var pinCode : String? = sharedPreferences.getString("pinCode","")
        if (getConnectionStatus() ){
            llProgressBar.visibility = View.VISIBLE
            getCategoryList(vendor_id,pinCode)
        }else{
           // progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }

        btnViewAllProduct.setOnClickListener {
            startActivity(Intent(this, GroceerFilterProductListActivity::class.java))
        }

    }

    private fun getCategoryList(vendorId: Int?, pinCode: String?) {

        val responseLiveData :LiveData<Response<Category>> = liveData {
            val rootObject= JSONObject()
            if (vendorId != null) {
                rootObject.put("vendorId",vendorId.toInt())
            }
            rootObject.put("pinCode",pinCode)
            rootObject.put("searchList","")

            Log.e("String",rootObject.toString())

            val response = groceerApiService.getCategoryList(rootObject.toString())

            emit(response)
        }

        responseLiveData.observe(this, Observer {
            val categoryResponseData =it?.body()
            if (categoryResponseData!= null){
                if (categoryResponseData.status.equals("200")){
                    val categoryListResponse: List<Objresult> = categoryResponseData.objresult
                    initAdapter(categoryListResponse)
                    llProgressBar.visibility = View.GONE
                }else{
                  //  progressBar.visibility = View.GONE
                    Toast.makeText(this,categoryResponseData.message.toString(),Toast.LENGTH_LONG).show()
                }

            }
        })
    }

    private fun initAdapter(categoryListResponse: List<Objresult>) {

        //categoryListView = categoryListView
        categoryListView.setHasFixedSize(true)
        categoryListView.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
                adapter = CategoryListAdater(context.applicationContext,categoryListResponse)

        }


      //  categoryListView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
