package com.GroceerCart.sa.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.VendorListAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.vendor.Vendor
import com.GroceerCart.sa.db.vendor.VendorDAO
import com.GroceerCart.sa.db.vendor.VendorDatabase
import com.GroceerCart.sa.db.vendor.VendorRepository
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.Table
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.Vendors
import kotlinx.android.synthetic.main.content_vendor_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class GroceerVendorListActivity : GroceerBaseActivity() {
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var pinCode : String
    private lateinit var vendorAdapter:VendorListAdapter
    private lateinit var searchVendor : SearchView
    private lateinit var progressBar: ProgressBar
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_vendor_list)
        setSupportActionBar(findViewById(R.id.toolbar))
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        progressBar = findViewById(R.id.vendorProgressBar)
        progressBar.visibility = View.VISIBLE

        pinCode = intent.getStringExtra("pinCode").toString()

        groceerApiService = GroceerApiInstance.getRetrofitInstance("5f53c55b").create(GroceerApiService::class.java)

        searchVendor = findViewById(R.id.searchView)

        vendorGridView.visibility = View.GONE

        if (getConnectionStatus() ){
            getVendorList()
        }else{
            progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",Toast.LENGTH_LONG).show()
        }

        searchVendor.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                vendorAdapter.filter.filter(newText)
                return false
            }

        })
    }

    private fun getVendorList() {
        val responseLiveData : LiveData<Response<Vendors>> = liveData {
            val rootObject= JSONObject()
            rootObject.put("seqno",1)
            rootObject.put("searchCode",pinCode)
            rootObject.put("condition","CHECKPIN")

            Log.e("String",rootObject.toString())

            val response = groceerApiService.postRawJSON(rootObject.toString())

            //val response = vendorService.getVendors(1,"500032","CHECKPIN")
            emit(response)
        }

        responseLiveData.observe(this, Observer {
            val table =it?.body()
            if (table!= null){
                if (table.status.equals("200")){
                    val editor:SharedPreferences.Editor =  sharedPreferences.edit()
                    editor.putBoolean("VendorFlag",true)
                    editor.commit()
                    editor.apply()
                    val table: List<Table> = table.objresult.table
                    initAdapter(table)
                }else{
                    progressBar.visibility = View.GONE
                    Toast.makeText(this,table.message.toString(),Toast.LENGTH_LONG).show()
                }

            }
        })
    }

    private fun initAdapter(table: List<Table>) {
      //  val adapter = VendorListAdapter(this,table)
        vendorAdapter = this?.let { VendorListAdapter(it,table) }!!
        vendorGridView.adapter = vendorAdapter
        progressBar.visibility = View.GONE
        vendorGridView.visibility  = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            addVendorDetailToDB(table)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    suspend fun addVendorDetailToDB(table: List<Table>) {
        var dao:VendorDAO = GroceerDatabase.getInstance(this).vendorDAO
        var repository = VendorRepository(dao)
        if (repository.getAllVendor().isNotEmpty()){
            repository.deleteAllVendor()
        }
        for ( vendor in table){
            repository.insertVendor(Vendor(null,vendor.seqno,vendor.branchId,vendor.clientAddress,vendor.clientCode,vendor.clientContact,
                vendor.clientEmail,vendor.clientImage,vendor.clientName,vendor.locationId,vendor.lastOrderDate,
                vendor.totalOrders,vendor.ratings,vendor.checkQuantity,vendor.locations_name,vendor.branch_name
            ))
        }

    }

}