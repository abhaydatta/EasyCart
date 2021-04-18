package com.GroceerCart.sa.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.GroceerCart.sa.R
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.db.User
import com.GroceerCart.sa.db.db.UserDAO
import com.GroceerCart.sa.db.db.UserDatabase
import com.GroceerCart.sa.db.db.UserRepository
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.*
import com.GroceerCart.sa.service.user.Table
import com.GroceerCart.sa.service.user.Table1
import com.GroceerCart.sa.service.user.UserDetail
import com.GroceerCart.sa.ui.OtpEditTextView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class GroceerOtpSingUpActivity : GroceerBaseActivity() {
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var mobileNo:String
    private lateinit var edtOtpText:OtpEditTextView
    private lateinit var mobileOtp:String
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText:TextView
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_otp_sing_up)


        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json,JSONObject::class.java)

        var vendor_code : String = vendorDetail.getString("clientCode")

        progressBar = findViewById(R.id.otpProgressBar)

        statusText = findViewById<TextView>(R.id.txtOtpSuccess)
        statusText.visibility = View.VISIBLE

        mobileNo = intent.getStringExtra("mobileno")!!

        var txtMobileNo = findViewById<TextView>(R.id.textMobileNumber)
        txtMobileNo.text = mobileNo

         edtOtpText  = findViewById<OtpEditTextView>(R.id.otpEditTextView)

        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(GroceerApiService::class.java)

        if (mobileNo.length>1){
            if (getConnectionStatus()){
                progressBar.visibility = View.VISIBLE
                sendOtp(mobileNo)
            }
        }else{
            progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",Toast.LENGTH_LONG).show()
        }


    }



      fun getOtpDetailList(v:View) {
        Log.e("API" ,"Fucntin call")
          progressBar.visibility = View.GONE
          statusText.visibility = View.GONE
          if (edtOtpText.getOTPText().toString().equals(mobileOtp)){
              registerUser(mobileNo)
             /* startActivity(Intent(this, GroceerHomeActivity::class.java))
              finish()*/
          }else{
              Toast.makeText(this,"OTP is not valid!",Toast.LENGTH_LONG).show()
          }
/*
*/
    }

    fun resendOtp(v:View){
        if(getConnectionStatus()){
            progressBar.visibility = View.VISIBLE
            val rootObject= JSONObject()
            rootObject.put("mobileNo",mobileNo)
            rootObject.put("otp",edtOtpText.getOTPText().toString())
            rootObject.put("condition","")

            Log.e("String",rootObject.toString())

            val responseLiveData : LiveData<Response<Otp>> = liveData {
                val response = groceerApiService.postSendOtpDetail(rootObject.toString())
                emit(response)
            }

            responseLiveData.observe(this, Observer {
                val otpResponseBody =it?.body()
                if (otpResponseBody!= null){
                    progressBar.visibility = View.GONE
                    Log.e("Table " , otpResponseBody.toString())
                    val otpDetail: List<OtpDetailTable> =otpResponseBody .objresult.table
                    val otpStatus : List<OtpStatusTable> = otpResponseBody.objresult.table1

                    if (otpStatus.get(0).msgStatus.equals("200")){

                        statusText.visibility = View.VISIBLE
                        statusText.text = otpStatus.get(0).msg.toString()
                        mobileOtp = otpDetail.get(0).otpnotif_otp.toString()
                    }

                }
            })
        }else{
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",Toast.LENGTH_LONG).show()

        }

    }

    fun sendOtp(mobileNo: String) {
        val rootObject= JSONObject()
        rootObject.put("mobileNo", mobileNo)
        rootObject.put("otp","")
        rootObject.put("condition","")

        Log.e("Request",rootObject.toString())

        val responseLiveData : LiveData<Response<Otp>> = liveData {
            val response = groceerApiService.postSendOtpDetail(rootObject.toString())
            emit(response)
        }

        responseLiveData.observe(this, Observer {
            val otpResponseBody =it?.body()
            if (otpResponseBody!= null){
                progressBar.visibility = View.GONE
                Log.e("Responce " , otpResponseBody.toString())
                val otpDetail: List<OtpDetailTable> =otpResponseBody .objresult.table
                val otpStatus : List<OtpStatusTable> = otpResponseBody.objresult.table1

                if (otpStatus.get(0).msgStatus.equals("200")){
                    val statusText = findViewById<TextView>(R.id.txtOtpSuccess)
                    statusText.visibility = View.VISIBLE
                    statusText.text = otpStatus.get(0).msg.toString()
                    mobileOtp = otpDetail.get(0).otpnotif_otp.toString()

                }

            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun changeNumber(view: View) {
        finish()
    }

    fun registerUser(mobileNo: String){

        val requestObj= JSONObject()
        requestObj.put("mobileNo", mobileNo)
        requestObj.put("condition","getcustomer")

        Log.e("Request",requestObj.toString())

        val responseLiveData : LiveData<Response<UserDetail>> = liveData {
            val response = groceerApiService.getReigsterdUserDetail(requestObj.toString())
            emit(response)
        }

        responseLiveData.observe(this, Observer {
            val otpResponseBody =it?.body()
            if (otpResponseBody!= null){
                progressBar.visibility = View.GONE
                Log.e("Responce " , otpResponseBody.toString())
                val responseStatus: List<Table> =otpResponseBody .objresult.table
                val userDetailResponse: List<Table1> =otpResponseBody .objresult.table1

                if (responseStatus.get(0).msgStatus.equals("200")){
                    CoroutineScope(Dispatchers.IO).launch {
                        addUserDetailToDB(userDetailResponse)
                    }
                }

            }
        })
    }

    suspend fun addUserDetailToDB(userDetailResponse: List<Table1>) {
        val dao: UserDAO = GroceerDatabase.getInstance(applicationContext).userDao
        val respository = UserRepository(dao)
        val userDetail: Table1 = userDetailResponse.get(0)

        respository.insertUser(
            User(
                userDetail.cartcustomer_id,
                userDetail.cartcustomer_firstName,
                userDetail.cartcustomer_lastName,
                userDetail.cartcustomer_emailAddress,
                userDetail.cartcustomer_contactNo,
                userDetail.cartcustomer_address1,
                userDetail.cartcustomer_address2,
                userDetail.cartcustomer_pinCode,
                userDetail.cartcustomer_isActive,
                userDetail.cartcustomer_isDeleted,
                userDetail.cartcustomer_createdDate,
                userDetail.cartcustomer_flogin,
                userDetail.cartcustomer_glogin,
                userDetail.cartcustomer_externalUserId,
                userDetail.cartcustomer_profilePic
            )
        )
        var intent : Intent = Intent()
        setResult(12,intent)
        finish()
    }

}