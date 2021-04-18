package com.GroceerCart.sa.activity.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.BuildConfig
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.activity.GroceerOtpSingUpActivity
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.db.User
import com.GroceerCart.sa.db.db.UserDAO
import com.GroceerCart.sa.db.db.UserDatabase
import com.GroceerCart.sa.db.db.UserRepository
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.user.Table
import com.GroceerCart.sa.service.user.Table1
import com.GroceerCart.sa.service.user.UserDetail
import com.GroceerCart.sa.ui.CircularImageView
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response


class LoginFragment : Fragment() {
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var progressBar: ProgressBar
    lateinit var callbackManager: CallbackManager
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences
    private lateinit var mobileNo:String
    private lateinit var mLoginLayout : ConstraintLayout
    private lateinit  var maccountLayout : LinearLayout
    private lateinit var edtMobileno : EditText
    private lateinit var facebook_login_btn : Button
    private lateinit var google_login_btn :Button

    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var recyclerView:RecyclerView
    private lateinit var userPic: String
    private lateinit var userName:TextView
    private lateinit var userProfile : CircularImageView

    lateinit var mActivity: GroceerHomeActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var root = inflater.inflate(R.layout.activity_groceer_login, container, false)

        mActivity = context as GroceerHomeActivity

         sharedPreferences = mActivity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json,JSONObject::class.java)

        var vendor_code : String = vendorDetail.getString("clientCode")
        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(GroceerApiService::class.java)


       // var loginMode : String? = sharedPreferences.getString("login_mode","")
          mLoginLayout = root.findViewById(R.id.layout_login)

         edtMobileno = root.findViewById<EditText>(R.id.edtMobileNo)
         progressBar = root.findViewById(R.id.loginProgressBar)

         edtMobileno.setOnEditorActionListener { _, actionId, _ ->
             if (actionId == EditorInfo.IME_ACTION_DONE) {
                 mobileNo = edtMobileno.text.toString()
                 if(mobileNo.length>0){
                     startActivityForResult(Intent(mActivity, GroceerOtpSingUpActivity::class.java).putExtra("mobileno",mobileNo),101)
                 }else{
                     Toast.makeText(activity,"Please Enter Mobile Number",Toast.LENGTH_LONG).show()
                 }
                 true
             }
             false
         }


         val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
             .requestIdToken(getString(R.string.google_web_application_id))
             .requestEmail()
             .build()

         mGoogleSignInClient = activity?.let { GoogleSignIn.getClient(it, gso) }!!

        google_login_btn = root.findViewById(R.id.img_google)

         google_login_btn.setOnClickListener {
             if (mActivity.getConnectionStatus()){
                 progressBar.visibility = View.VISIBLE

                 signIn()
             }else{
                 progressBar.visibility = View.GONE
                 Toast.makeText(getActivity(),"Network Connection Error! , Please check your Internet Connection and Try Again",
                     Toast.LENGTH_LONG).show()
             }

         }

         callbackManager = CallbackManager.Factory.create()

         /* if (isLoggedIn()) {
             // Show the Activity with the logged in user
         }else{
             // Show the Home Activity
             startActivity(Intent(activity, GroceerLoginActivity::class.java))
         }*/


    /*    facebook_login_btn.setOnClickListener {
            if (mActivity.getConnectionStatus()){
                progressBar.visibility = View.VISIBLE
                LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
            }else{
                progressBar.visibility = View.GONE
                Toast.makeText(getActivity(),"Network Connection Error! , Please check your Internet Connection and Try Again",
                    Toast.LENGTH_LONG).show()
            }

        }*/

        // Callback registration
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                Log.d("TAG", "Success Login")
                progressBar.visibility = View.GONE
                // Get User's Info
                getUserProfile(loginResult?.accessToken, loginResult?.accessToken?.userId)

            }

            override fun onCancel() {
                Toast.makeText(activity, "Login Cancelled", Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()
            }
        })

        return root
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(
            signInIntent, RC_SIGN_IN
        )
    }

    fun isLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        return isLoggedIn
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("onActivityResult" , "Fargment")
        if (requestCode == RC_SIGN_IN) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        if (resultCode == 12 || requestCode == 12){
            val editor:SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putBoolean("login_mode",true)
            mActivity.addMenuItem()
            mActivity.removeMenuItem(R.id.nav_login)
            (activity?.findViewById<View>(R.id.bottom_navigation_view) as BottomNavigationView).selectedItemId = R.id.nav_account

        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(
                ApiException::class.java
            )

            val editor:SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putBoolean("login_mode",true)
            // Signed in successfully
            val googleId = account?.id ?: ""
            Log.i("Google ID", googleId)

            val googleFirstName = account?.givenName ?: ""
            Log.i("Google First Name", googleFirstName)

            editor.putString("login_google_username",googleFirstName)


            val googleLastName = account?.familyName ?: ""
            Log.i("Google Last Name", googleLastName)

            val googleEmail = account?.email ?: ""
            Log.i("Google Email", googleEmail)

            val googleProfilePicURL = account?.photoUrl.toString()
            Log.i("Google Profile Pic URL", googleProfilePicURL)
            editor.putString("login_google_userPic",googleProfilePicURL)
            editor.apply()
            editor.commit()
            val googleIdToken = account?.idToken ?: ""
            Log.i("Google ID Token", googleIdToken)

            val rootObject= JSONObject()
            rootObject.put("emailAddress", googleEmail)
            rootObject.put("glogin",true)
            rootObject.put("externalUserId",googleId)
            rootObject.put("condition","getcustomer")


            registerUser(rootObject)

           // (activity?.findViewById<View>(R.id.bottom_navigation_view) as BottomNavigationView).selectedItemId = R.id.nav_account

           // startActivity(Intent(activity, GroceerHomeActivity::class.java))


        } catch (e: ApiException) {
            // Sign in was unsuccessful
            Log.e(
                "failed code=", e.toString()
            )
        }
    }

    fun registerUser(requestObj : JSONObject){

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
                   //addUserDetailToDB(userDetailResponse)
                   }
                mActivity.addMenuItem()
                mActivity.removeMenuItem(R.id.nav_login)
                (activity?.findViewById<View>(R.id.bottom_navigation_view) as BottomNavigationView).selectedItemId = R.id.nav_account


            }
        })
    }

    suspend fun addUserDetailToDB(userDetailResponse: List<Table1>) {
        val dao: UserDAO = GroceerDatabase.getInstance(mActivity.applicationContext).userDao
        val respository = UserRepository(dao)
        val userDetail: Table1 = userDetailResponse.get(0)
        var editor:SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("userId",userDetail.cartcustomer_id)
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
       }

    private fun signOut() {
        activity?.let {
            mGoogleSignInClient.signOut()
                .addOnCompleteListener(it) {
                    // Update your UI here
                }
        }
    }

    @SuppressLint("LongLogTag")
    fun getUserProfile(token: AccessToken?, userId: String?) {

        val parameters = Bundle()
        parameters.putString(
            "fields",
            "id, first_name, middle_name, last_name, name, picture, email"
        )
        GraphRequest(token,
            "/$userId/",
            parameters,
            HttpMethod.GET,
            GraphRequest.Callback { response ->
                val jsonObject = response.jsonObject
                val editor:SharedPreferences.Editor =  sharedPreferences.edit()
                editor.putString("login_mode","facebook")

                // Facebook Access Token
                // You can see Access Token only in Debug mode.
                // You can't see it in Logcat using Log.d, Facebook did that to avoid leaking user's access token.
                if (BuildConfig.DEBUG) {
                    FacebookSdk.setIsDebugEnabled(true)
                    FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS)
                }

                // Facebook Id
                if (jsonObject.has("id")) {
                    val facebookId = jsonObject.getString("id")
                    Log.i("Facebook Id: ", facebookId.toString())
                } else {
                    Log.i("Facebook Id: ", "Not exists")
                }


                // Facebook First Name
                if (jsonObject.has("first_name")) {
                    val facebookFirstName = jsonObject.getString("first_name")
                    Log.i("Facebook First Name: ", facebookFirstName)
                } else {
                    Log.i("Facebook First Name: ", "Not exists")
                }


                // Facebook Middle Name
                if (jsonObject.has("middle_name")) {
                    val facebookMiddleName = jsonObject.getString("middle_name")
                    Log.i("Facebook Middle Name: ", facebookMiddleName)
                } else {
                    Log.i("Facebook Middle Name: ", "Not exists")
                }


                // Facebook Last Name
                if (jsonObject.has("last_name")) {
                    val facebookLastName = jsonObject.getString("last_name")
                    Log.i("Facebook Last Name: ", facebookLastName)
                } else {
                    Log.i("Facebook Last Name: ", "Not exists")
                }


                // Facebook Name
                if (jsonObject.has("name")) {
                    val facebookName = jsonObject.getString("name")
                    Log.i("Facebook Name: ", facebookName)
                    editor.putString("login_fb_username",facebookName)

                } else {
                    Log.i("Facebook Name: ", "Not exists")
                }


                // Facebook Profile Pic URL
                if (jsonObject.has("picture")) {
                    val facebookPictureObject = jsonObject.getJSONObject("picture")
                    if (facebookPictureObject.has("data")) {
                        val facebookDataObject = facebookPictureObject.getJSONObject("data")
                        if (facebookDataObject.has("url")) {
                            val facebookProfilePicURL = facebookDataObject.getString("url")
                            Log.i("Facebook Profile Pic URL: ", facebookProfilePicURL)
                            editor.putString("login_fb_userPic",facebookProfilePicURL)

                        }
                    }
                } else {
                    Log.i("Facebook Profile Pic URL: ", "Not exists")
                }

                // Facebook Email
                if (jsonObject.has("email")) {
                    val facebookEmail = jsonObject.getString("email")
                    Log.i("Facebook Email: ", facebookEmail)
                    editor.putString("login_fb_email",facebookEmail)

                } else {
                    Log.i("Facebook Email: ", "Not exists")
                }
                editor.apply()
                editor.commit()
               // (activity?.findViewById<View>(R.id.bottom_navigation_view) as BottomNavigationView).selectedItemId = R.id.nav_account

                // startActivity(Intent(activity, GroceerHomeActivity::class.java))



            }).executeAsync()
    }





}