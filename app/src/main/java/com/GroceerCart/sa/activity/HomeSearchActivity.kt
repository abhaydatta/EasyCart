package com.GroceerCart.sa.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.CategoryProductAdapter
import com.GroceerCart.sa.adapter.UomAdapter
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
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.notify.Notify
import com.GroceerCart.sa.service.productitem.ProductItem
import com.GroceerCart.sa.service.productitem.Table
import com.GroceerCart.sa.service.productitem.UomProducts
import com.facebook.FacebookSdk
import com.google.gson.Gson
import kotlinx.android.synthetic.main.content_home_search.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode

class HomeSearchActivity : GroceerBaseActivity(),CategoryProductAdapter.productListener, UomAdapter.UomListener {
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var productListAdapter: CategoryProductAdapter
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences
    private lateinit var searchGridView:GridView
    private lateinit var imgScanner: ImageView
    private lateinit var produuctListResponse: List<Table>
    private  var vendor_id :Int = 0
    private var itemPrice:Double = 0.0
    private var  dialog: Dialog? = null
    private var checkQantity: Boolean = false
    private var uomParentProductPosition:Int = 0
    private lateinit var uomList:List<UomProducts>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_search)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        searchGridView = findViewById(R.id.searchGridView)
        imgScanner = findViewById(R.id.searchScanner)

        imgScanner.setOnClickListener {
            startActivityForResult(Intent(this, GroceerBarcodeScannerActivity::class.java),501)
        }

        var category_id = intent.getIntExtra("categoryId",0)
        var subCateogry_id = intent.getIntExtra("subCategoryId",0)

        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendor_code : String  = vendorDetail.getString("clientCode")
        vendor_id   = vendorDetail.getInt("seqno")
        var pinCode : String? = sharedPreferences.getString("pinCode","")
        checkQantity = sharedPreferences.getBoolean("checkQuantity",false)

        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(GroceerApiService::class.java)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (getConnectionStatus() ){
                    if (newText!!.isEmpty()){
                        produuctListResponse = ArrayList()
                        initAdapter(produuctListResponse)
                    }else{
                        llProgressBar.visibility = View.VISIBLE
                        getProductSearchList(newText.toString().trim(),vendor_id,category_id,pinCode)
                    }
                }else{
                    llProgressBar.visibility = View.GONE
                    Toast.makeText(this@HomeSearchActivity,"Network Connection Error! , Please check your Internet Connection and Try Again",
                        Toast.LENGTH_LONG).show()
                }
              /*  if (newText.toString().length >= 3){
                    if (getConnectionStatus() ){
                        getProductSearchList(newText.toString().trim(),vendor_id,category_id,pinCode)
                    }else{
                        //progressBar.visibility = View.GONE
                        Toast.makeText(this@HomeSearchActivity,"Network Connection Error! , Please check your Internet Connection and Try Again",
                            Toast.LENGTH_LONG).show()
                    }
                }else{
                    if (getConnectionStatus() ){
                        getProductSearchList(" ",vendor_id,category_id,pinCode)
                    }else{
                        //progressBar.visibility = View.GONE
                        Toast.makeText(this@HomeSearchActivity,"Network Connection Error! , Please check your Internet Connection and Try Again",
                            Toast.LENGTH_LONG).show()
                    }
                }*/
                return false
            }

        })
    }

    private fun getProductSearchList(
        searchString:String,
        vendorId: Int, categoryId: Int, pinCode: String?
    ) {
        var responseLiveData : LiveData<Response<ProductItem>> = liveData {
            val searchArray = JSONArray()
            val searchObject = JSONObject()
            searchObject.put("typeId",4)
            searchObject.put("keyword",searchString)
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
                     produuctListResponse = productResponseData.objresult.table
                    initAdapter(produuctListResponse)
                    llProgressBar.visibility = View.GONE
                }else{
                    llProgressBar.visibility = View.GONE
                    Toast.makeText(this,productResponseData.message.toString(), Toast.LENGTH_LONG).show()
                }

            }
        })
    }

    private fun initAdapter(produuctListResponse: List<Table>) {
        productListAdapter = this?.let { CategoryProductAdapter(it,produuctListResponse,this,checkQantity) }!!
        searchGridView.adapter = productListAdapter
        productListAdapter.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun insertItemToDb(position: Int, itemQty: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            var listTax: List<Tax> = getAllTax(produuctListResponse.get(position).products_parentId)
            addItemCartToDb(produuctListResponse.get(position),itemQty,listTax)
        }
    }

    override fun updateItemToDb(position: Int, itemQty: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            var listTax: List<Tax> = getAllTax(produuctListResponse.get(position).products_parentId)
            var productItem : List<Item> = getAllCartItem(produuctListResponse.get(position).productId)
            updateItemCartToDb(productItem.get(0), itemQty,listTax)
        }
    }

    override fun uomOpenDialog(position: Int) {
        uomParentProductPosition = position
        uomList = produuctListResponse.get(uomParentProductPosition).uomProducts
        if(uomList.isNotEmpty()){
            showDialog(uomList)
        }
    }
    suspend fun addItemCartToDb(
        item: Table,
        itemQty: Int,
        taxPriceList: List<Tax>
    ) {
        var taxPrice: Double = 0.0
        var cartItemPrice:Double = 0.0
        var taxItemPrice:Double = 0.0
        if (item.offerPrice > 0){
            cartItemPrice = item.offerPrice
            itemPrice = item.offerPrice
        }else{
            cartItemPrice = item.sellingPrice
            itemPrice = item.sellingPrice
        }

        for(tax in taxPriceList){
            if (tax.taxType.equals("Percentage")){
                if (item.offerPrice > 0){
                    taxItemPrice = (item.offerPrice / 100) * tax.taxValue
                    taxPrice += taxItemPrice
                }else{
                    taxItemPrice = (item.sellingPrice / 100) * tax.taxValue
                    taxPrice += taxItemPrice
                }
            }else{
                taxItemPrice = tax.taxValue
                taxPrice +=  taxItemPrice
            }
            taxItemPrice = BigDecimal(taxItemPrice).setScale(2, RoundingMode.HALF_UP).toDouble()

            var taxDao : TaxItemDAO = GroceerDatabase.getInstance(this).taxItemDAO
            var taxRepository = TaxItemRepository(taxDao)
            taxRepository.insertTaxItem(TaxItem(
                null,item.productId,vendor_id,tax.seqno,tax.name,tax.taxType,tax.taxValue,taxItemPrice,tax.groupId)
            )
        }
        cartItemPrice *= itemQty
        cartItemPrice = BigDecimal(cartItemPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        taxPrice *= itemQty
        taxPrice = BigDecimal(taxPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        var dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        var repository = ItemRepository(dao)
        repository.insertItem(
            Item(null,item.productId,item.products_parentId,item.products_categoryId,
                item.products_subcategoryId,vendor_id,1,item.name," ",
                " "," ", item.imagePath,item.cartStockInHand,item.rating,
                item.offerPrice,item.sellingPrice,
                itemPrice,itemQty,cartItemPrice,taxPrice,0.0)
        )
    }

    private suspend fun updateItemCartToDb(
        item: Item,
        itemQty: Int,
        listTax: List<Tax>
    ) {
        var taxPrice: Double = 0.0
        var taxItemPrice: Double = 0.0
        var cartItemPrice:Double = 0.0
        if (item.offerPrice > 0){
            cartItemPrice = item.offerPrice
            // taxPrice = item.taxAmount
        }else{
            cartItemPrice = item.sellingPrice
            // taxPrice = item.taxAmount
        }
        for(tax in listTax){
            if (tax.taxType.equals("Percentage")){
                if (item.offerPrice > 0){
                    taxItemPrice = (item.offerPrice / 100) * tax.taxValue
                    taxPrice += taxItemPrice
                }else{
                    taxItemPrice = (item.sellingPrice / 100) * tax.taxValue
                    taxPrice += taxItemPrice
                }
            }else{
                taxItemPrice = tax.taxValue
                taxPrice +=  taxItemPrice
            }
        }
        taxPrice *= itemQty
        cartItemPrice *= itemQty
        var dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        var repository = ItemRepository(dao)
        repository.updateItemWithParameter(itemQty,cartItemPrice,taxPrice,item.productId)

        var taxItemPriceNew: Double = 0.0
        var taxDao: TaxItemDAO = GroceerDatabase.getInstance(this).taxItemDAO
        var taxRepository = TaxItemRepository(taxDao)
        var taxItemDbList: List<TaxItem> = taxRepository.getTaxItemWithProductId(item.productId)
        for (taxItem in taxItemDbList) {
            if (taxItem.taxType.equals("Percentage")) {
                if (item.offerPrice > 0) {
                    taxItemPriceNew = (item.offerPrice / 100) * taxItem.taxValue
                    //taxPriceNew += taxItemPriceNew
                } else {
                    taxItemPriceNew = (item.sellingPrice / 100) * taxItem.taxValue
                    // taxPriceNew += taxItemPriceNew
                }
            } else {
                taxItemPriceNew = taxItem.taxValue
                // taxPriceNew += taxItemPriceNew
            }
            taxItemPriceNew   *= itemQty
            taxItem.id?.let { taxRepository.updateTaxWithId(taxItemPriceNew, it) }
        }
    }


    suspend fun getAllTax(productParentId: Int): List<Tax> {
        val dao: TaxDAO = GroceerDatabase.getInstance(this).taxDAO
        val respository = TaxRepository(dao)
        var tax:List<Tax> = respository.getTaxWithProductParentId(productParentId)

        return tax
    }

    suspend fun getAllCartItem(productId: Int):List<Item>{
        val dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        val respository = ItemRepository(dao)
        var item:List<Item> = respository.getItemWithId(productId)
        return item
    }

    suspend fun deleteItemFromCart(productId:Int){
        val dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        val respository = ItemRepository(dao)
        respository.deleteById(productId)
    }

    fun showDialog(uomList: List<UomProducts>) {
        dialog = Dialog(this)
        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.layout_uom)
        val btndialog =
            dialog?.findViewById(R.id.uomClose) as Button
        btndialog.setOnClickListener { dialog!!.dismiss() }
        val recyclerView: RecyclerView = dialog!!.findViewById(R.id.uomRecyclerView)
        val adapterRe = UomAdapter(this, uomList, checkQantity,this)
        recyclerView.adapter = adapterRe
        recyclerView.layoutManager = LinearLayoutManager(
            FacebookSdk.getApplicationContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.setOnClickListener { }
        dialog!!.show()
        val window: Window? = dialog!!.getWindow()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun addItemCartUom(position: Int, itemQty: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            var listTax: List<Tax> = getAllTax(uomList.get(position).products_parentId)
            addItemCartToDbUom(uomList.get(position),itemQty,listTax)
        }
    }

    override fun updateItemCartUom(position: Int, itemQty: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            var listTax: List<Tax> = getAllTax(uomList.get(position).products_parentId)
            var productItem : List<Item> = getAllCartItem(uomList.get(position).productId)
            updateItemCartToDb(productItem.get(0), itemQty,listTax)
        }    }


    private suspend fun addItemCartToDbUom(
        item: UomProducts,
        itemQty: Int,
        taxPriceList: List<Tax>
    ) {
        var taxPrice: Double = 0.0
        var cartItemPrice:Double = 0.0
        var taxItemPrice:Double = 0.0
        if (item.offerPrice > 0){
            cartItemPrice = item.offerPrice
            itemPrice = item.offerPrice
        }else{
            cartItemPrice = item.sellingPrice
            itemPrice = item.sellingPrice
        }

        for(tax in taxPriceList){
            if (tax.taxType.equals("Percentage")){
                if (item.offerPrice > 0){
                    taxItemPrice = (item.offerPrice / 100) * tax.taxValue
                    taxPrice += taxItemPrice
                }else{
                    taxItemPrice = (item.sellingPrice / 100) * tax.taxValue
                    taxPrice += taxItemPrice
                }
            }else{
                taxItemPrice = tax.taxValue
                taxPrice +=  taxItemPrice
            }
            taxItemPrice = BigDecimal(taxItemPrice).setScale(2, RoundingMode.HALF_UP).toDouble()

            var taxDao : TaxItemDAO = GroceerDatabase.getInstance(this).taxItemDAO
            var taxRepository = TaxItemRepository(taxDao)
            taxRepository.insertTaxItem(TaxItem(
                null,item.productId,vendor_id,tax.seqno,tax.name,tax.taxType,tax.taxValue,taxItemPrice,tax.groupId)
            )
        }
        cartItemPrice *= itemQty
        cartItemPrice = BigDecimal(cartItemPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        taxPrice *= itemQty
        taxPrice = BigDecimal(taxPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        var dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        var repository = ItemRepository(dao)
        repository.insertItem(
            Item(null,item.productId,item.products_parentId,item.products_categoryId,
                item.products_subcategoryId,vendor_id,1,item.name," ",
                " "," ", item.imagePath,item.cartStockInHand,item.rating,
                item.offerPrice,item.sellingPrice,
                itemPrice,itemQty,cartItemPrice,taxPrice,0.0)
        )
    }

    override fun notifyProduct(position: Int) {
        callNotify(position)
    }


    override fun notifyUom(position: Int) {
        callNotify(position)
    }

    private fun callNotify(position: Int) {
        var responseLiveData : LiveData<Response<Notify>> = liveData {
            val rootObject= JSONObject()
            rootObject.put("seqno",0)
            rootObject.put("productId",produuctListResponse.get(position).productId)
            rootObject.put("customerId",getUserId())

            Log.e("String",rootObject.toString())

            val response = groceerApiService.updateNotifyMe(rootObject.toString())
            emit(response)
        }

        responseLiveData.observe(this, Observer {
            val notifyResponseData =it?.body()
            if (notifyResponseData!= null){
                if (notifyResponseData.status.equals("200")){
                    var message:String = notifyResponseData.message
                    Toast.makeText(this,message, Toast.LENGTH_LONG).show()
                    // progressBar.visibility = View.GONE
                }else{
                    //progressBar.visibility = View.GONE
                    Toast.makeText(this,notifyResponseData.message, Toast.LENGTH_LONG).show()
                }

            }
        })
    }

    private suspend fun getUserId(): Int {
        val dao: UserDAO = GroceerDatabase.getInstance(this).userDao
        val respository = UserRepository(dao)
        var user:List<User> = respository.getAllUsers()
        return user.get(0).id!!
    }
}