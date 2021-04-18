package com.GroceerCart.sa.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.GroceerCart.sa.R
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.addAddress.AddAddress
import com.GroceerCart.sa.service.country.Country
import com.GroceerCart.sa.service.country.Table
import com.GroceerCart.sa.ui.CountryDialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_groceer_add_user_address.*
import org.json.JSONObject
import retrofit2.Response

class GroceerAddUserAddressActivity : GroceerBaseActivity(),CountryDialogFragment.onDialogClick {
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var btnSaveAddress:Button
    private lateinit var  countryList : List<Table>
    private  var countryId : Int = 0
    private  var customerId : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_add_user_address)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        customerId = intent.getIntExtra("userId",0)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendor_code : String = vendorDetail.getString("clientCode")
        var pinCode : String? = sharedPreferences.getString("pinCode","")
        edtPinCode.setText(pinCode)
        edtPinCode.keyListener = null
        edtCountry.keyListener = null
        var userFirstName:String = edtCustomerFistName.text.toString()
        var userLastName:String = edtCustomerLastName.text.toString()
        var userAddress:String = edtAddress.text.toString()
        var userCity : String = edtCity.text.toString()
        var userState :String = edtState.text.toString()
        var userContact: String = edtContactNumber.text.toString()
        var userEmailId: String = edtEmailId.text.toString()



        var userCountry = findViewById<EditText>(R.id.edtCountry)

        userCountry.setOnClickListener {
            var dialog = CountryDialogFragment(countryList,this)
            dialog.show(supportFragmentManager,"countryDialog")

        }

        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(
            GroceerApiService::class.java)

        if (getConnectionStatus() ){
            getCountryList()
        }else{
            // progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }

        btnSaveAddress = findViewById(R.id.btnSave)

        btnSaveAddress.setOnClickListener {
            if (getConnectionStatus() ){
                saveAddress(vendor_code,pinCode,customerId)
            }else{
                // progressBar.visibility = View.GONE
                Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveAddress(
        vendorCode: String,
        pinCode: String?,
        customerId: Int
    ) {
        var responseLiveData : LiveData<Response<AddAddress>> = liveData {
            val rootObject= JSONObject()
            rootObject.put("seno",0)
            rootObject.put("customerId",customerId)
            rootObject.put("firstName",edtCustomerFistName.text.toString())
            rootObject.put("lastName",edtCustomerLastName.text.toString())
            rootObject.put("countryId",countryId)
            rootObject.put("state",edtState.text.toString())
            rootObject.put("city",edtCity.text.toString())
            rootObject.put("pinCode",pinCode)
            rootObject.put("address",edtAddress.text.toString())
            rootObject.put("mobileNo",edtContactNumber.text.toString())
            rootObject.put("emmailAddress",edtEmailId.text.toString())
            rootObject.put("condition","saveadd")

            Log.e("Request",rootObject.toString())

            val response = groceerApiService.saveCustomerAddress(rootObject.toString())
            Log.e("Response",response.toString())
            emit(response)
        }
        responseLiveData.observe(this, Observer {
            val userAddressResponseData =it?.body()
            if (userAddressResponseData != null) {
                if (userAddressResponseData.status.equals("200")){
                    Toast.makeText(this,userAddressResponseData.objresult.table.get(0).msg.toString(),Toast.LENGTH_LONG).show()
                    var intent = Intent()
                    intent.putExtra("vendorId",vendorCode)
                    intent.putExtra("cId",customerId)
                    setResult(401,intent)
                    finish()
                }
            }
        })
    }

    fun getCountryList(){
        var responseLiveData : LiveData<Response<Country>> = liveData {
            val rootObject= JSONObject()
            rootObject.put("type",77)

            Log.e("Request",rootObject.toString())

            val response = groceerApiService.getCountryList(rootObject.toString())
            Log.e("Response",response.toString())
            emit(response)
        }
        responseLiveData.observe(this, Observer {
            val countryResponseData =it?.body()
            if (countryResponseData != null) {
                if (countryResponseData.status.equals("200")){
                    countryList  = countryResponseData.objresult.table
                }
            }
        })
    }

    override fun dialogClick(position: Int) {
        edtCountry.setText(countryList.get(position).country_name)
        countryId = countryList.get(position).country_id
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

