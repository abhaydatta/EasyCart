package com.GroceerCart.sa.filter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.FilterCategoryAdapter
import com.GroceerCart.sa.adapter.FilterRatingAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.categoryservice.Category
import com.GroceerCart.sa.service.categoryservice.Objresult
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.rizlee.rangeseekbar.RangeSeekBar
import org.json.JSONObject
import retrofit2.Response

class GroceerFilterProductListActivity : GroceerBaseActivity(),
    RangeSeekBar.OnRangeSeekBarRealTimeListener, FilterCategoryAdapter.onCheckPosition, FilterRatingAdapter.onCheckRatingId{
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var filterCategoryAdapter: FilterCategoryAdapter
    private lateinit var filterCategoryRecyclerView : RecyclerView
    private lateinit var filterRatingAdapter: FilterRatingAdapter
    private lateinit var filterRatingRecyclerView : RecyclerView
    private lateinit var btnFilter : Button
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences
    private var mAppBarConfiguration: AppBarConfiguration? = null
     lateinit var drawerLayout: DrawerLayout
    var category_id: Int = 0
      var categoryList : String = ""
      var ratingList : String=""
      var priceList : String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_filter)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)



        drawerLayout = findViewById(R.id.filter_drawer_layout)


        var rangeSeekBar : RangeSeekBar = findViewById(R.id.rangeSeekBar)
        rangeSeekBar.listenerRealTime = this

        val drawerLayout: DrawerLayout = findViewById(R.id.filter_drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_filter_view)

        val navController = findNavController(R.id.nav_host_filter_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_filter_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, mAppBarConfiguration!!)
        navView.setupWithNavController(navController)

        category_id = intent.getIntExtra("categoryId",0)
        var subCateogry_id = intent.getIntExtra("subCategoryId",0)

        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendor_code : String  = vendorDetail.getString("clientCode")
        var vendor_id :Int  = vendorDetail.getInt("seqno")
        var vendor_name = vendorDetail.getString("clientName")

        var location_id = vendorDetail.getString("locationId")
        var branch_id = vendorDetail.getString("branchId")

        var pinCode : String? = sharedPreferences.getString("pinCode","")

        var toolbarText = findViewById<TextView>(R.id.txtToolbarVendorTitle)
        toolbarText.text = vendor_name

        filterCategoryRecyclerView = findViewById(R.id.filter_cat_recycler_view)
        filterCategoryRecyclerView.setHasFixedSize(true)
        var filterCategoryLayout = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        filterCategoryRecyclerView.layoutManager = filterCategoryLayout


        filterRatingRecyclerView = findViewById(R.id.filter_rating_recycler_view)
        filterRatingRecyclerView.setHasFixedSize(true)
        var filterRatingLayout = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        filterRatingRecyclerView.layoutManager = filterRatingLayout
        /*filterRatingAdapter = FilterRatingAdapter(this,this)
        filterRatingRecyclerView.adapter = filterRatingAdapter*/

        btnFilter = findViewById(R.id.btnFilter)
        btnFilter.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            var selectedFragment: androidx.fragment.app.Fragment? = null
            selectedFragment = ProductFilterFragment()
            supportFragmentManager.beginTransaction().replace(
                R.id.nav_host_filter_fragment,
                selectedFragment
            ).commit()
            /*var fragment = supportFragmentManager.findFragmentById(R.id.nav_filter_home) as ProductFilterFragment
            fragment.callme()*/
        }
        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(GroceerApiService::class.java)

        if (getConnectionStatus() ){
           // getAllProductList(vendor_id,category_id,pinCode)
            getCategoryList(vendor_id,pinCode)
        }else{
            //progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }

    }

    fun openFilterDrawer(){
            drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun getCategoryList(vendorId: Int?, pinCode: String?) {

        val responseLiveData :LiveData<Response<Category>> = liveData {
            val rootObject= JSONObject()
            if (vendorId != null) {
                rootObject.put("vendorId",vendorId)
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
                    initCategoryAdapter(categoryListResponse)
                }else{
                    //  progressBar.visibility = View.GONE
                    Toast.makeText(this,categoryResponseData.message.toString(),Toast.LENGTH_LONG).show()
                }

            }
        })
    }

    private fun initCategoryAdapter(categoryListResponse: List<Objresult>) {
        filterCategoryAdapter = FilterCategoryAdapter(this,categoryListResponse,this)
        filterCategoryRecyclerView.adapter = filterCategoryAdapter
        initRatingAdapter()
    }

    private fun initRatingAdapter() {
        filterRatingAdapter = FilterRatingAdapter(this,this)
        filterRatingRecyclerView.adapter = filterRatingAdapter

    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_filter_fragment)
        return mAppBarConfiguration?.let { navController.navigateUp(it) }!! || super.onSupportNavigateUp()
    }

    override fun onCheckCategory(categroyId: Int) {
        if (categoryList.length < 1){
            categoryList  = categroyId.toString()
        }else{
            categoryList =   categoryList + "," + categroyId.toString()
        }
    }

    override fun onValuesChanging(minValue: Float, maxValue: Float) {
        priceList = minValue.toString() + "," + maxValue.toString()
    }

    override fun onValuesChanging(minValue: Int, maxValue: Int) {
        priceList = minValue.toString() + "," + maxValue.toString()
    }

    override fun onCheckRating(ratingId: Int) {
        ratingList = ratingId.toString()
    }


}