package com.GroceerCart.sa.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.ui.AccountFragment
import com.GroceerCart.sa.activity.ui.LoginFragment
import com.GroceerCart.sa.activity.ui.cart.GroceerCartFragment
import com.GroceerCart.sa.activity.ui.home.HomeFragment
import com.GroceerCart.sa.activity.ui.vendor.VendorFragment
import com.GroceerCart.sa.adapter.ExpandableMenuListAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.filter.GroceerFilterProductListActivity
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.homeservice.Table
import com.GroceerCart.sa.service.homeservice.Table6
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item.view.*
import org.json.JSONObject


class GroceerHomeActivity : GroceerBaseActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var appBarConfiguration: AppBarConfiguration
     var vendor_id:Int = 0
    lateinit var vendor_name:String
    lateinit var vendor_code :String
    var vendor_locationId:Int = 0
    var vendor_branchId:Int = 0
    private lateinit var menuListAdapter: ExpandableMenuListAdapter
    private lateinit var menuListView: ExpandableListView
    private lateinit var headerList: List<Table>
    private lateinit var childList:HashMap<Table,List<Table6>>
    private lateinit var groceerApiService: GroceerApiService
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences
    private lateinit var  navBottomView : BottomNavigationView
    val REQUEST_CODE = 11
    val RESULT_CODE = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json,JSONObject::class.java)

        vendor_code = vendorDetail.getString("clientCode")
        vendor_id = vendorDetail.getInt("seqno")
        vendor_name = vendorDetail.getString("clientName")
        vendor_branchId = vendorDetail.getInt("branchId")
        vendor_locationId = vendorDetail.getInt("locationId")

        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(GroceerApiService::class.java)

        menuListView = findViewById(R.id.expandableListView)

       /* vendor_id = intent.getStringExtra("vendor_id").toString()

        vendor_name = intent.getStringExtra("vendor_name").toString()*/


        var toolbarText = findViewById<TextView>(R.id.txtToolbarVendorTitle)
        toolbarText.text = vendor_name


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
         navBottomView  = findViewById(R.id.bottom_navigation_view)
        navBottomView.setOnNavigationItemSelectedListener(navListener);
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navBottomView.setupWithNavController(navController)
        // Finding the Navigation Controller
        var loginMode:Boolean = sharedPreferences.getBoolean("login_mode",false)
        if (loginMode){
            removeMenuItem(R.id.nav_login)
        }else{
            removeMenuItem(R.id.nav_account)
        }
    }

     fun prepareMenuData(headerData: List<Table>, childData: List<Table6>) {
        headerList = headerData
        childList = HashMap<Table,List<Table6>>()
        for (header in headerList){
            var childListArray:MutableList<Table6> = ArrayList()
            for (child in childData){
                if (header.seqno.equals(child.categoryId)){
                    childListArray.add(child)
                }
            }
            childList[header] = childListArray
        }
        populateExpandableList(headerList,childList)

    }

    fun populateExpandableList(headerList: List<Table>, childList: HashMap<Table, List<Table6>>
    ) {
        menuListAdapter = ExpandableMenuListAdapter(this, this.headerList, this.childList)
        menuListView!!.setAdapter(menuListAdapter)
        menuListView!!.setOnGroupClickListener { parent, v, groupPosition, id ->
          /*  if (this.headerList[groupPosition].isGroup) {
                if (!this.headerList[groupPosition].hasChildren) {
                   *//* val webView: WebView = findViewById(R.id.webView)
                    webView.loadUrl(headerList[groupPosition].url)*//*
                    onBackPressed()
                }
            }*/
            false
        }
        menuListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            if (this.childList[this.headerList[groupPosition]] != null) {
                val model =
                    this.childList[this.headerList[groupPosition]]!![childPosition]
                var mIntent : Intent = Intent(this, GroceerFilterProductListActivity::class.java)
                mIntent.putExtra("categoryId",headerList[groupPosition].seqno)
                mIntent.putExtra("subCategoryId",model.seqno)
                this.startActivityForResult(mIntent,505)
            }
            false
        }
    }

    private val navListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_vendor -> selectedFragment = VendorFragment()
                R.id.nav_cart -> selectedFragment = GroceerCartFragment()
                R.id.nav_login -> selectedFragment = LoginFragment()
                R.id.nav_account -> selectedFragment = AccountFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.nav_host_fragment,
                    selectedFragment
                ).commit()
            }
            true
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
       // menuInflater.inflate(R.menu.groceer_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

    fun hideBottomNavigationItem(id:Int) {
        try {
            val menuView = navBottomView.getChildAt(0) as BottomNavigationMenuView
            menuView.findViewById<View>(id).visibility = View.GONE
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun showBottomNavigationItem(id: Int, view: BottomNavigationView) {
        val menuView = navBottomView.getChildAt(0) as BottomNavigationMenuView
        menuView.findViewById<View>(id).visibility = View.VISIBLE
        menuView.findViewById<View>(id).title
    }

    fun removeMenuItem(id:Int){
        navBottomView.getMenu().removeItem(id)

    }

    fun addMenuItem(){
        val menu: Menu = navBottomView.getMenu()
        menu.add(
            Menu.NONE,
            R.id.nav_account,
            Menu.NONE,
            getString(R.string.menu_setting)
        )
            .setIcon(R.drawable.ic_settings)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("onActivityResult" , "Activity")
        if (requestCode === REQUEST_CODE && resultCode === RESULT_CODE) {
           removeMenuItem(R.id.nav_login)
            addMenuItem()
        }
    }

}