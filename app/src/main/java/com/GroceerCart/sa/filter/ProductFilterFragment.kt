package com.GroceerCart.sa.filter

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.notify.Notify
import com.GroceerCart.sa.service.productitem.ProductItem
import com.GroceerCart.sa.service.productitem.Table
import com.GroceerCart.sa.service.productitem.UomProducts
import com.facebook.FacebookSdk.getApplicationContext
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_product_filter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode


/**
 * A simple [Fragment] subclass.
 * Use the [ProductFilterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductFilterFragment : Fragment(), CategoryProductAdapter.productListener ,UomAdapter.UomListener{
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var productListAdapter: CategoryProductAdapter
    private val sharedPrefFile = "groceerpreference"
    lateinit var activity : GroceerFilterProductListActivity
    private lateinit var btnFilter:Button
    private lateinit var  sharedPreferences: SharedPreferences
    private lateinit var  produuctListResponse: List<Table>
    private  var vendor_id :Int = 0
    private lateinit var priceSymbol:String
    private var decimal:Int = 1
    private lateinit var progressBar: ProgressBar
    private var itemPrice:Double = 0.0
    private var  dialog: Dialog? = null
    private var checkQantity: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_product_filter, container, false)
         activity  = context as GroceerFilterProductListActivity

        progressBar = root.findViewById(R.id.appPogressBar)

        btnFilter = root.findViewById(R.id.btnFilter)

        btnFilter.setOnClickListener {
            activity.openFilterDrawer()
        }

     /*   var category_id = intent.getIntExtra("categoryId",0)
        var subCateogry_id = intent.getIntExtra("subCategoryId",0)*/

        sharedPreferences = activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendor_code : String  = vendorDetail.getString("clientCode")
        vendor_id   = vendorDetail.getInt("seqno")
        checkQantity = vendorDetail.getBoolean("checkQuantity")

        var pinCode : String? = sharedPreferences.getString("pinCode","")
        priceSymbol = sharedPreferences.getString("priceSymbol","").toString()
        decimal = sharedPreferences.getInt("decimal",2)


        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(GroceerApiService::class.java)

        if (activity.getConnectionStatus() ){
            progressBar.visibility = View.VISIBLE
            getAllProductList(activity.categoryList,activity.priceList,activity.ratingList,vendor_id,activity.category_id,pinCode)
        }else{
            progressBar.visibility = View.GONE
            Toast.makeText(activity,"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }

        return root
    }

    private fun getAllProductList(
        categoryList:String,priceList:String,rangeList:String,
        vendorId: Int,
        categoryId: Int,
        pinCode: String?
    ) {
        var responseLiveData : LiveData<Response<ProductItem>> = liveData {
            val rootObject= JSONObject()

            val searchArray = JSONArray()
            val searchObject = JSONObject()
            if (categoryList.length>0){
                val searchCatObject = JSONObject()
                searchCatObject.put("typeId",1)
                searchCatObject.put("keyword",categoryList)
                searchArray.put(searchCatObject)
            }else{
                if (categoryId>0){
                    searchObject.put("typeId",1)
                    searchObject.put("keyword",categoryId)
                    searchArray.put(searchObject)
                }
            }


            if (priceList.length>0){
                val searchCatObject = JSONObject()
                searchCatObject.put("typeId",2)
                searchCatObject.put("keyword",priceList)
                searchArray.put(searchCatObject)
            }

            if (rangeList.length>0){
                val searchRatingObject = JSONObject()
                searchRatingObject.put("typeId",3)
                searchRatingObject.put("keyword",rangeList)
                searchArray.put(searchRatingObject)
            }

            rootObject.put("vendorId",vendorId)
            rootObject.put("pinCode",pinCode)
            rootObject.put("searchList",searchArray)
            rootObject.put("pageNo",1)
            rootObject.put("pageCount",50)

            Log.e("String",rootObject.toString())

            val response = groceerApiService.getProductList(rootObject.toString())
            emit(response)
        }

        responseLiveData.observe(viewLifecycleOwner, Observer {
            val productResponseData =it?.body()
            if (productResponseData!= null){
                if (productResponseData.status.equals("200")){
                     produuctListResponse = productResponseData.objresult.table
                    initAdapter(produuctListResponse)
                    progressBar.visibility = View.GONE
                }else{
                     progressBar.visibility = View.GONE
                    Toast.makeText(activity,productResponseData.message.toString(), Toast.LENGTH_LONG).show()
                }

            }
        })
    }


    private fun initAdapter(produuctListResponse: List<Table>) {
        activity.priceList = ""
        activity.ratingList = ""
        activity.categoryList = ""
        productListAdapter = activity?.applicationContext?.let { CategoryProductAdapter(it,produuctListResponse,this,checkQantity) }!!
        filterListGridView.adapter = productListAdapter
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
        var uomCount:Int = produuctListResponse.get(position).uomProductsCount
        if(uomCount > 0){
            showDialog(produuctListResponse.get(position).uomProducts)
        }
    }

    override fun notifyProduct(position: Int) {
        callNotify(position)
    }

    private suspend fun addItemCartToDb(
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
            var taxDao : TaxItemDAO = GroceerDatabase.getInstance(activity).taxItemDAO
            var taxRepository = TaxItemRepository(taxDao)
            taxRepository.insertTaxItem(TaxItem(
                null,item.productId,vendor_id,tax.seqno,tax.name,tax.taxType,tax.taxValue,taxItemPrice,tax.groupId)
            )
        }
        cartItemPrice *= itemQty
        cartItemPrice = BigDecimal(cartItemPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        taxPrice *= itemQty
        taxPrice = BigDecimal(taxPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        var dao: ItemDAO = GroceerDatabase.getInstance(activity).itemDAO
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
        var dao: ItemDAO = GroceerDatabase.getInstance(activity).itemDAO
        var repository = ItemRepository(dao)
        repository.updateItemWithParameter(itemQty,cartItemPrice,taxPrice,item.productId)

        var taxItemPriceNew: Double = 0.0
        var taxDao: TaxItemDAO = GroceerDatabase.getInstance(activity).taxItemDAO
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
        val dao: TaxDAO = GroceerDatabase.getInstance(activity).taxDAO
        val respository = TaxRepository(dao)
        var tax:List<Tax> = respository.getTaxWithProductParentId(productParentId)

        return tax
    }

    suspend fun getAllCartItem(productId: Int):List<Item>{
        val dao: ItemDAO = GroceerDatabase.getInstance(activity).itemDAO
        val respository = ItemRepository(dao)
        var item:List<Item> = respository.getItemWithId(productId)
        return item
    }

    suspend fun deleteItemFromCart(productId:Int){
        val dao: ItemDAO = GroceerDatabase.getInstance(activity).itemDAO
        val respository = ItemRepository(dao)
        respository.deleteById(productId)
    }

    fun showDialog(uomList: List<UomProducts>) {
        dialog = getActivity()?.let { Dialog(it) }
        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.layout_uom)
        val btndialog =
            dialog?.findViewById(R.id.uomClose) as Button
        btndialog.setOnClickListener { dialog!!.dismiss() }
        val recyclerView: RecyclerView = dialog!!.findViewById(R.id.uomRecyclerView)
        val adapterRe = getActivity()?.applicationContext?.let { UomAdapter(it, uomList,checkQantity,this) }
        recyclerView.adapter = adapterRe
        recyclerView.layoutManager = LinearLayoutManager(
            getApplicationContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
     //   recyclerView.setOnClickListener { }
        dialog!!.show()
        val window: Window? = dialog!!.getWindow()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun addItemCartUom(position: Int,itemQty: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            var listTax: List<Tax> = getAllTax(produuctListResponse.get(position).products_parentId)
            addItemCartToDb(produuctListResponse.get(position),itemQty,listTax)
        }
    }

    override fun updateItemCartUom(position: Int, itemQty: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            var listTax: List<Tax> = getAllTax(produuctListResponse.get(position).products_parentId)
            var productItem : List<Item> = getAllCartItem(produuctListResponse.get(position).productId)
            updateItemCartToDb(productItem.get(0), itemQty,listTax)
        }    }

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

        responseLiveData.observe(viewLifecycleOwner, Observer {
            val notifyResponseData =it?.body()
            if (notifyResponseData!= null){
                if (notifyResponseData.status.equals("200")){
                    var message:String = notifyResponseData.message
                    Toast.makeText(activity,message, Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }else{
                    progressBar.visibility = View.GONE
                    Toast.makeText(activity,notifyResponseData.message, Toast.LENGTH_LONG).show()
                }

            }
        })
    }

    private suspend fun getUserId(): Int {
        val dao: UserDAO = GroceerDatabase.getInstance(activity).userDao
        val respository = UserRepository(dao)
        var user:List<User> = respository.getAllUsers()
        return user.get(0).id!!
    }
}