package com.GroceerCart.sa.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.OrderListAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.cart.ItemDAO
import com.GroceerCart.sa.db.cart.ItemRepository
import com.GroceerCart.sa.db.db.User
import com.GroceerCart.sa.db.db.UserDAO
import com.GroceerCart.sa.db.db.UserRepository
import com.GroceerCart.sa.db.db.cart.Item
import com.GroceerCart.sa.db.tax.Tax
import com.GroceerCart.sa.db.tax.TaxDAO
import com.GroceerCart.sa.db.tax.TaxRepository
import com.GroceerCart.sa.db.taxitem.TaxItem
import com.GroceerCart.sa.db.taxitem.TaxItemDAO
import com.GroceerCart.sa.db.taxitem.TaxItemRepository
import com.GroceerCart.sa.db.vendor.Vendor
import com.GroceerCart.sa.db.vendor.VendorDAO
import com.GroceerCart.sa.db.vendor.VendorRepository
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.placeorder.PlaceOrder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_groceer_order_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

class GroceerOrderListActivity : GroceerBaseActivity() {
    private lateinit var groceerApiService: GroceerApiService
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences
    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var tvOrderNetTotal : TextView
    private lateinit var btnPlaceOrder : Button
    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private var layoutManager: RecyclerView.LayoutManager? = null
    lateinit var  itemCartList :List<Item>
    lateinit var  cartVendorList : MutableList<Vendor>
    lateinit var cartSubTotal:MutableList<Double>
    lateinit var cartTotal:MutableList<Double>
    private var orderTotal:Double = 0.0
    private var orderSubTotal:Double = 0.0
    private lateinit var taxIdList:HashMap<Int,List<Int>>
    private var netOrderTotal:Double = 0.047
    private lateinit var headJsonObject : JSONObject
    private var vendorId:Int = 0
    private var customerId:Int? = 0
    private var addressId:Int = 0
    private var slotId:Int = 0
    private var priceSymbol: String? = null
    private var decimalPoint: Int = 2
    private var pinCode : String? = null
    private lateinit var userObject : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_order_list)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendorCode : String  = vendorDetail.getString("clientCode")
        vendorId   = vendorDetail.getInt("seqno")
        var location_id = vendorDetail.getString("locationId")
        var branch_id = vendorDetail.getString("branchId")
        pinCode  = sharedPreferences.getString("pinCode","")
        addressId = sharedPreferences.getInt("addressId",0)
        //slotId = sharedPreferences.getInt("deliveryslots_id",0)
         priceSymbol = sharedPreferences.getString("priceSymbol","")
         decimalPoint = sharedPreferences.getInt("decimal",2)


        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendorCode).create(GroceerApiService::class.java)
        layoutManager = LinearLayoutManager(this)
        orderRecyclerView = findViewById(R.id.orderRecyclerView)
        orderRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        orderRecyclerView.setHasFixedSize(true)
        orderRecyclerView.layoutManager = layoutManager

        tvOrderNetTotal = findViewById(R.id.tvOrderNetTotal)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)

        CoroutineScope(Dispatchers.Main).launch {
            userObject = getUserId()
        }

        btnPlaceOrder.setOnClickListener {
            if (getConnectionStatus() ){
                var addressFlag:Boolean = sharedPreferences.getBoolean("addressFlag",false)
                if (addressFlag){
                    var gson = Gson()
                    var slotIdString: String? = sharedPreferences.getString("deliveryslots_id","")
                    if(slotIdString != null){
                        val type =
                            object : TypeToken<HashMap<String?, String?>?>() {}.type
                        val slotListMap: HashMap<Int, Int> =
                            gson.fromJson(slotIdString, type)
                        if (slotListMap.isNotEmpty()){
                            placeOrder(slotListMap)
                        }else{
                            Toast.makeText(this,"Please Select Delivery Slot for this Order !",
                                Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(this,"Please Select Delivery Slot for this Order !",
                            Toast.LENGTH_LONG).show()
                    }

                }else{
                    Toast.makeText(this,"Please add Delivery Address for this Order !",
                        Toast.LENGTH_LONG).show()
                }
            }else{
                //progressBar.visibility = View.GONE
                Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                    Toast.LENGTH_LONG).show()
            }
        }

        btnDeliveryAddress.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                customerId = userObject.id
                startActivity(Intent(this@GroceerOrderListActivity,GroceerAddressListActivity::class.java).putExtra("customerId",customerId))
            }
        }

        btnDeliverySlot.setOnClickListener {
            startActivity(Intent(this@GroceerOrderListActivity,GroceerDeliverySlotActivity::class.java))
        }

        CoroutineScope(Dispatchers.Main).launch {
            var vendorList : List<Vendor> = getAllVendor()
            cartVendorList = ArrayList()
            cartSubTotal = ArrayList()
            cartTotal = ArrayList()
            for (vendor in vendorList) {
                var itemList : List<Item> = getItemByVendorId(vendor.seqno)
                if(itemList.isNotEmpty()){
                    for (item in itemList){
                        orderSubTotal += item.cartPrice
                        orderTotal += item.cartPrice + item.taxAmount
                    }
                    cartVendorList.add(vendor)
                    cartSubTotal.add(orderSubTotal)
                    cartTotal.add(orderTotal)
                    orderSubTotal = 0.0
                    orderTotal = 0.0
                }
            }
            loadAdapter(cartVendorList,cartSubTotal,cartTotal)
        }

        CoroutineScope(Dispatchers.IO).launch {
           itemCartList = getAllCartItem()
            for (itemCart in itemCartList){
                netOrderTotal += itemCart.cartPrice + itemCart.taxAmount
            }
            tvOrderNetTotal.setText(priceSymbol + String.format("%.2f", netOrderTotal).toDouble().toString())
       }
    }

    private fun placeOrder(slotListMap: HashMap<Int, Int>) {
        CoroutineScope(Dispatchers.Main).launch {
            var responseLiveData : LiveData<Response<PlaceOrder>> = liveData {
                var netAllTotal : Double = 0.0
                var vendorList : List<Vendor> = getAllVendor()

                headJsonObject  = JSONObject()
                headJsonObject.put("customerId",customerId)
                headJsonObject.put("addressId",addressId)

                var cartArray = JSONArray()

                for (vendor in vendorList){
                    var itemList : List<Item> = getItemByVendorId(vendor.seqno)
                    var cartJSONObject = JSONObject()
                    if (itemList.isNotEmpty()){

                        //cartJSONObject.put("orderNo",1)

                       // cartJSONObject.put("salesPersonId",1)
                       // cartJSONObject.put("orderStatus","")
                        //cartJSONObject.put("transactionDate",1)
                        //cartJSONObject.put("orderType","")
                        //cartJSONObject.put("narration"," ")
                        //cartJSONObject.put("roundOff",16)
                        //cartJSONObject.put("buffer2","")
                       /* cartJSONObject.put("buffer3","")
                        cartJSONObject.put("buffer4","")
                        cartJSONObject.put("buffer5","")
                        cartJSONObject.put("buffer6","")*/


                        // cartJSONObject.put("masterOrderId",27)
                       // cartJSONObject.put("deliverySlotId", slotListMap[vendor.seqno])

                        //cartJSONObject.put("estDeliveryDate","2020-11-04T17:31:53.7967624 05:30")

                        var billinJSONArray = JSONArray()
                        var billinTaxJSONArray = JSONArray()
                        var taxAmount :Double = 0.0
                        var cartItemPrice:Double = 0.0
                        var netTotalAmount = 0.0
                        for (item in itemList){
                            taxAmount += item.taxAmount
                            cartItemPrice += item.cartPrice
                            netTotalAmount = cartItemPrice + taxAmount
                            var billingJSONObject = JSONObject()
                            billingJSONObject.put("productId",item.productId)
                            billingJSONObject.put("productName",item.productName)
                           // billingJSONObject.put("reasonId",1)
                            billingJSONObject.put("price",item.offerPrice)
                            billingJSONObject.put("quantity",item.cartItem)
                            billingJSONObject.put("amount",item.cartPrice)
                           // billingJSONObject.put("remarks"," ")
                            billingJSONObject.put("categoryId",item.categoryId)
                            //billingJSONObject.put("categoryName",item.categoryName)
                            billingJSONObject.put("subcategoryId",item.subCategoryId)
                           // billingJSONObject.put("subcategoryName",item.subCategoryName)

                            var listTax: List<Tax> = getAllTax(item.productsParentId)
                            for (tax in listTax){
                                var billingTaxJSONObject = JSONObject()
                                billingTaxJSONObject.put("taxTypeId",tax.id)
                                billingTaxJSONObject.put("amount",2)
                                billinTaxJSONArray.put(billingTaxJSONObject)
                            }

                            billinJSONArray.put(billingJSONObject)

                        }
                        netAllTotal += netTotalAmount
                        cartJSONObject.put("billingdetails",billinJSONArray)
                        cartJSONObject.put("billingtaxes",billinTaxJSONArray)
                        cartJSONObject.put("seqno",0)
                        cartJSONObject.put("clientId",1)
                        cartJSONObject.put("clientCode",vendor.clientCode)
                        cartJSONObject.put("locationId",vendor.locationId)
                        cartJSONObject.put("branchId",vendor.branchId)
                        cartJSONObject.put("customerId",userObject.id)
                        cartJSONObject.put("totalAmount",cartItemPrice)
                        cartJSONObject.put("netTotal",netTotalAmount)
                        cartJSONObject.put("taxAmount",taxAmount)
                        cartJSONObject.put("discountAmount",0)
                        cartJSONObject.put("customerName",userObject.firstName + userObject.lastName)
                        cartJSONObject.put("customerEmail",userObject.emailAddress)
                        cartJSONObject.put("customerMobile",userObject.contactNumber)
                        cartJSONObject.put("customerAddress",userObject.address1)
                        cartJSONObject.put("customerZipCode",pinCode)
                        cartJSONObject.put("deliverySlotId", 15)
                    }
                    cartArray.put(cartJSONObject)
                }

                headJsonObject.put("cartDetails",cartArray)
               // headJsonObject.put("masterOrderNo",1)
                headJsonObject.put("totalAmount",netAllTotal)
               // headJsonObject.put("orderStatus",1)
                var deliverySlotJArray = JSONArray()
            /*    deliverySlotJArray.put("")
                deliverySlotJArray.put("")*/
                headJsonObject.put("selectedDeliverySlots",deliverySlotJArray)

                val response = groceerApiService.placeOrder(headJsonObject.toString())
                emit(response)

            }

            responseLiveData.observe(this@GroceerOrderListActivity, Observer {
                val orderResponseData =it?.body()
                if (orderResponseData!= null){
                    if (orderResponseData.status.equals("200")){
                        Toast.makeText(this@GroceerOrderListActivity,"Order Placed Successfully !", Toast.LENGTH_LONG).show()
                        CoroutineScope(Dispatchers.IO).launch {
                            clearCartItem()
                        }
                        startActivity(Intent(this@GroceerOrderListActivity,GroceerHomeActivity::class.java))
                        finish()
                    }
                }else{
                    //  progressBar.visibility = View.GONE
                    Toast.makeText(this@GroceerOrderListActivity,orderResponseData?.message.toString(), Toast.LENGTH_LONG).show()
                }

            })
        }
    }

    suspend fun getAllCartItem():List<Item>{
        var itemCartListItem : List<Item> = ArrayList()
        val dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        val respository = ItemRepository(dao)
        itemCartListItem = respository.getAllItem()
        return itemCartListItem
    }

    private fun loadAdapter(
        vendorCart: MutableList<Vendor>,
        orderSubTotal: MutableList<Double>,
        orderTotal: MutableList<Double>
    ) {
        orderRecyclerView.setHasFixedSize(true)
        orderRecyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = OrderListAdapter(this@GroceerOrderListActivity,vendorCart,priceSymbol,orderTotal,orderSubTotal)
        }
    }

    override suspend fun getAllVendor():List<Vendor>{
        super.getAllVendor()
        val dao: VendorDAO = GroceerDatabase.getInstance(this).vendorDAO
        val respository = VendorRepository(dao)
        var vendorList:List<Vendor> = respository.getAllVendor()
        return vendorList
    }

    override suspend fun getItemByVendorId(vendorId: Int):List<Item>{
        super.getItemByVendorId(vendorId)
        val dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        val respository = ItemRepository(dao)
        var item:List<Item> = respository.getItemWithVendorId(vendorId)
        return item
    }

    suspend fun getAllTax(productParentId: Int): List<Tax> {
        val dao: TaxDAO = GroceerDatabase.getInstance(this).taxDAO
        val respository = TaxRepository(dao)
        var tax:List<Tax> = respository.getTaxWithProductParentId(productParentId)

        return tax
    }

    private suspend fun clearCartItem(){
        val dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        val repository = ItemRepository(dao)
        val item : List<Item> = repository.getAllItem()
        if (item.isNotEmpty()){
            repository.deleteAllItem()
        }

        val taxDao: TaxItemDAO = GroceerDatabase.getInstance(this).taxItemDAO
        val taxRepository = TaxItemRepository(taxDao)
        val taxItems : List<TaxItem> = taxRepository.getAllTaxItem()
        if (taxItems.isNotEmpty()){
            taxRepository.deleteAllTaxItem()
        }
    }

    private suspend fun getUserId(): User {
        val dao: UserDAO = GroceerDatabase.getInstance(this).userDao
        val respository = UserRepository(dao)
        var user:List<User> = respository.getAllUsers()
        return user.get(0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getRequestAPI(slotListMap: HashMap<Int, Int>):String{
        CoroutineScope(Dispatchers.Main).launch {

            var vendorList : List<Vendor> = getAllVendor()

            headJsonObject  = JSONObject()
            headJsonObject.put("customerId",customerId)
            headJsonObject.put("addressId",addressId)

            var cartArray = JSONArray()

            for (vendor in vendorList){
                var itemList : List<Item> = getItemByVendorId(vendor.seqno)
                var cartJSONObject = JSONObject()
                if (itemList.size > 0){
                    cartJSONObject.put("seqno",0)
                    cartJSONObject.put("orderNo",1)
                    cartJSONObject.put("clientId",1)
                    cartJSONObject.put("clientCode",vendor.clientCode)
                    cartJSONObject.put("locationId",vendor.locationId)
                    cartJSONObject.put("branchId",vendor.branchId)
                    cartJSONObject.put("customerId",1)
                    cartJSONObject.put("totalAmount",1)
                    cartJSONObject.put("netTotal",1)
                    cartJSONObject.put("taxAmount",1)
                    cartJSONObject.put("discountAmount",1)
                    cartJSONObject.put("salesPersonId",1)
                    cartJSONObject.put("orderStatus","")
                    cartJSONObject.put("transactionDate",1)
                    cartJSONObject.put("orderType","")
                    cartJSONObject.put("narration"," ")
                    cartJSONObject.put("roundOff",16)
                    cartJSONObject.put("buffer2","")
                    cartJSONObject.put("buffer3","")
                    cartJSONObject.put("buffer4","")
                    cartJSONObject.put("buffer5","")
                    cartJSONObject.put("buffer6","")
                    cartJSONObject.put("customerName","abhay")
                    cartJSONObject.put("customerEmail","a")
                    cartJSONObject.put("customerMobile","1234567890")
                    cartJSONObject.put("customerAddress","")
                    cartJSONObject.put("customerZipCode","")
                    cartJSONObject.put("masterOrderId",27)
                    cartJSONObject.put("deliverySlotId", slotListMap[vendor.seqno])
                    cartJSONObject.put("estDeliveryDate","2020-11-04T17:31:53.7967624 05:30")


                    var billinJSONArray = JSONArray()
                    var billinTaxJSONArray = JSONArray()
                    for (item in itemList){
                        var billingJSONObject = JSONObject()
                        billingJSONObject.put("productId",item.productId)
                        billingJSONObject.put("productName",item.productName)
                        billingJSONObject.put("reasonId",1)
                        billingJSONObject.put("price",item.offerPrice)
                        billingJSONObject.put("quantity",item.cartItem)
                        billingJSONObject.put("amount",item.cartPrice)
                        billingJSONObject.put("remarks"," ")
                        billingJSONObject.put("categoryId",item.categoryId)
                        billingJSONObject.put("categoryName",item.categoryName)
                        billingJSONObject.put("subcategoryId",item.subCategoryId)
                        billingJSONObject.put("subcategoryName",item.subCategoryName)

                        var listTax: List<Tax> = getAllTax(item.productsParentId)
                        for (tax in listTax){
                            var billingTaxJSONObject = JSONObject()
                            billingTaxJSONObject.put("taxTypeId",tax.id)
                            billingTaxJSONObject.put("amount",2)
                            billinTaxJSONArray.put(billingTaxJSONObject)
                        }

                        billinJSONArray.put(billingJSONObject)

                    }
                    cartJSONObject.put("billingdetails",billinJSONArray)
                    cartJSONObject.put("billingtaxes",billinTaxJSONArray)

                }
                cartArray.put(cartJSONObject)
            }

            headJsonObject.put("cartDetails",cartArray)
            headJsonObject.put("masterOrderNo",1)
            headJsonObject.put("totalAmount",1)
            headJsonObject.put("orderStatus",1)
            var deliverySlotJArray = JSONArray()
            deliverySlotJArray.put("")
            deliverySlotJArray.put("")
            headJsonObject.put("selectedDeliverySlots",deliverySlotJArray)

        }
        return headJsonObject.toString()
    }

}