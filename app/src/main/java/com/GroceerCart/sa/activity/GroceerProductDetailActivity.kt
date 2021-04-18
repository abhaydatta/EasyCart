package com.GroceerCart.sa.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.ProductThumbnailAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.cart.ItemDAO
import com.GroceerCart.sa.db.cart.ItemRepository
import com.GroceerCart.sa.db.db.cart.Item
import com.GroceerCart.sa.db.tax.Tax
import com.GroceerCart.sa.db.tax.TaxDAO
import com.GroceerCart.sa.db.tax.TaxRepository
import com.GroceerCart.sa.db.taxitem.TaxItem
import com.GroceerCart.sa.db.taxitem.TaxItemDAO
import com.GroceerCart.sa.db.taxitem.TaxItemRepository
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.productDetail.ProductDetail
import com.GroceerCart.sa.service.productDetail.Table
import com.GroceerCart.sa.service.productDetail.Table1
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_groceer_product_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode

class GroceerProductDetailActivity : GroceerBaseActivity(),ProductThumbnailAdapter.onClickPosition {
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var thumbnail_adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var  productThumbnailResponse : List<Table1>
    private lateinit var btnProductDetail:Button
    private lateinit var btnProductDescription:Button
    private var itemQty : Int = 0
    private lateinit var itemListDb:List<Item>
    private lateinit var produuctListResponse: List<Table>
    private var productId : Int = 0
    private var vendor_id : Int = 0
    private var itemPrice:Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_product_detail)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        productId  = intent.getIntExtra("productId",0)
        
        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)

        var vendor_code : String = vendorDetail.getString("clientCode")
        vendor_id  = vendorDetail.getInt("seqno")

        val thumbnailRecyclerView = findViewById<RecyclerView>(R.id.recycler_thumbnail)
        thumbnailRecyclerView.setHasFixedSize(true)
        var horizontalLayout = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        thumbnailRecyclerView.layoutManager = horizontalLayout

        groceerApiService = GroceerApiInstance.getRetrofitInstance(vendor_code).create(
            GroceerApiService::class.java)

        if (getConnectionStatus() ){
            getProductDetail(vendor_id,productId)
        }else{
            // progressBar.visibility = View.GONE
            Toast.makeText(this,"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }

        var btnCartAdd: Button = findViewById(R.id.btnProdAddToCart)
        var cartView:RelativeLayout = findViewById(R.id.detailCartAddView)
        val cartItemQtyAdd = findViewById<Button>(R.id.btnpQtyAdd)
        val cartItemQtyRemove = findViewById<Button>(R.id.btnpQtyMinus)
        val edtItemQty = findViewById<EditText>(R.id.edtpQty)

        CoroutineScope(Dispatchers.Main).launch {
            itemListDb = getAllCartItem(productId)
            if (itemListDb!= null && itemListDb.size >0){
                btnCartAdd.visibility = View.GONE
                cartView.visibility = View.VISIBLE
                itemQty = itemListDb.get(0).cartItem
                edtItemQty.setText(itemQty.toString())
            }else{
                itemQty = 0
            }
        }

        cartItemQtyAdd.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(productId)
                var item : List<Item> = getAllCartItem(productId)
                if (itemListDb!= null && itemListDb.isNotEmpty()){
                    itemQty = itemListDb[0].cartItem + 1
                    edtItemQty.setText(itemQty.toString())
                    Log.e("Cart qty Add","cart add more than  cart itemm - " + itemQty)
                    var listTax: List<Tax> = getAllTax(item.get(0).productsParentId)
                  //  updateItemCartToDb(item.get(0),itemQty )
                    updateItemCartToDb(item.get(0), itemQty,listTax)

                }else{
                    itemQty = 1
                    edtItemQty.setText(itemQty.toString())
                    Log.e("Cart qty  Add","cart add one  cart itemm" + itemQty)
                    var listTax: List<Tax> = getAllTax(item.get(0).productsParentId)
                    updateItemCartToDb(item.get(0),itemQty,listTax )
                }
            }
        }

        cartItemQtyRemove.setOnClickListener {

            CoroutineScope(Dispatchers.Main).launch {
                var item : List<Item> = getAllCartItem(productId)
                itemListDb = getAllCartItem(productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(0).cartItem
                    if (itemQty == 1){
                        btnCartAdd.visibility = View.VISIBLE
                        cartView.visibility = View.GONE
                        deleteItemFromCart(productId)
                    }else{
                        itemQty -= 1
                        edtItemQty.setText(itemQty.toString())
                        var listTax: List<Tax> = getAllTax(item[0].productsParentId)
                        updateItemCartToDb(item.get(0), itemQty,listTax)
                    }
                }
            }
        }

        btnCartAdd.setOnClickListener {
            var loginMode:Boolean = sharedPreferences.getBoolean("login_mode",false)
            if(loginMode){
                itemQty = 0
                btnCartAdd.visibility = View.GONE
                cartView.visibility = View.VISIBLE
                itemQty += 1
                edtItemQty.setText(itemQty.toString())
                CoroutineScope(Dispatchers.IO).launch {
                    var listTax: List<Tax> = getAllTax(produuctListResponse[0].products_parentId)
                    addItemCartToDb(produuctListResponse[0],itemQty,listTax)
                }
            }else{
                Toast.makeText(this@GroceerProductDetailActivity,"Please Login first before adding item to the cart !" , Toast.LENGTH_LONG).show()
            }
        }


    }

    private fun getProductDetail(vendorId: Int, productId: Int) {
        var responseLiveData :LiveData<Response<ProductDetail>> = liveData {
            val rootObject= JSONObject()
            rootObject.put("productId",productId)
            rootObject.put("vendorId",vendorId)
            Log.e("Request",rootObject.toString())

            val response = groceerApiService.getProductDetail(rootObject.toString())
            Log.e("Response",response.toString())
            emit(response)
        }
        responseLiveData.observe(this, Observer {
            val productDetailResponseData =it?.body()
            if (productDetailResponseData!= null){
                if (productDetailResponseData.status.equals("200")){
                     produuctListResponse = productDetailResponseData.objresult.table
                     productThumbnailResponse  = productDetailResponseData.objresult.table1
                    setViews(produuctListResponse,productThumbnailResponse)
                }else{
                    //  progressBar.visibility = View.GONE
                    Toast.makeText(this,productDetailResponseData.message.toString(),Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun setViews(
        produuctListResponse: List<Table>,
        productThumbnailResponse: List<Table1>
    ) {
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(this).load(/*GroceerApiInstance.BASE_URL1 +*/ produuctListResponse.get(0).imagePath)
            .apply(requestOptions).into(ivProductDetail)
        tvProductDetailTitle.text = produuctListResponse.get(0).name

        var offerPrice : Double = produuctListResponse.get(0).offerPrice
        if (offerPrice > 0){
            tvProductDetailActualPrice.visibility = View.VISIBLE
            tvProductDetailOfferPrice.visibility = View.VISIBLE
            crossViewProduuctDetail.visibility = View.VISIBLE
            tvProductDetailOfferPrice.text = produuctListResponse.get(0).symbol +  produuctListResponse.get(0).offerPrice.toString()
            tvProductDetailActualPrice.text = produuctListResponse.get(0).symbol +  produuctListResponse.get(0).sellingPrice.toString()
        }else{
            tvProductDetailActualPrice.visibility = View.VISIBLE
            tvProductDetailOfferPrice.visibility = View.VISIBLE
            crossViewProduuctDetail.visibility = View.VISIBLE
            tvProductDetailActualPrice.text = produuctListResponse.get(0).symbol +  produuctListResponse.get(0).offerPrice.toString()
            tvProductDetailOfferPrice.text = produuctListResponse.get(0).symbol +  produuctListResponse.get(0).sellingPrice.toString()
        }


        ratingProduct.rating = produuctListResponse.get(0).rating.toFloat()
        if(productThumbnailResponse.size > 0){
            recycler_thumbnail.visibility = View.VISIBLE
            loadProductThemubnailItemView(productThumbnailResponse)
        }else{
            recycler_thumbnail.visibility = View.GONE
        }

        btnProductDetail = findViewById(R.id.btnProdDetail)
        btnProductDescription = findViewById(R.id.btnProdDesc)

        btnProductDetail.setOnClickListener {
            btnProductDescription.setBackgroundColor(resources.getColor(android.R.color.transparent))
            btnProductDetail.setBackgroundColor(resources.getColor(R.color.color_white_text))
            productDetailWebView.visibility = View.GONE
            tvProductDetail.visibility = View.VISIBLE
            if (produuctListResponse.get(0).description != null){
                tvProductDetail.text = produuctListResponse.get(0).description
            }

        }


        btnProductDescription.setOnClickListener {
            productDetailWebView.visibility = View.VISIBLE
            tvProductDetail.visibility = View.GONE
            btnProductDetail.setBackgroundColor(resources.getColor(android.R.color.transparent))
            btnProductDescription.setBackgroundColor(resources.getColor(R.color.color_white_text))
            productDetailWebView.requestFocus()
            if (produuctListResponse.get(0).productInfo != null){
                productDetailWebView.loadData(produuctListResponse.get(0).productInfo.toString(),"text/html","UTF-8")
            }
        }

    }

    private fun loadProductThemubnailItemView(productThumbnailResponse: List<Table1>) {
        thumbnail_adapter = ProductThumbnailAdapter(this,productThumbnailResponse,this)
        recycler_thumbnail.adapter = thumbnail_adapter
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onClick(position: Int) {
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(this).load(GroceerApiInstance.BASE_URL + productThumbnailResponse.get(position).filepath)
            .apply(requestOptions).into(ivProductDetail)
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

            var taxDao : TaxItemDAO = GroceerDatabase.getInstance(this).taxItemDAO
            var taxRepository = TaxItemRepository(taxDao)
            taxRepository.insertTaxItem(TaxItem(
                null,productId,vendor_id,tax.seqno,tax.name,tax.taxType,tax.taxValue,taxItemPrice,tax.groupId)
            )
        }
        cartItemPrice *= itemQty
        cartItemPrice = BigDecimal(cartItemPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        taxPrice *= itemQty
        taxPrice = BigDecimal(taxPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        var dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        var repository = ItemRepository(dao)
        repository.insertItem(
            Item(null,productId,item.products_parentId,item.categoryId,
                item.subcategoryId,vendor_id,1,item.name,item.categoryName,
                item.subcategoryName," ", item.imagePath,item.cartStockInHand,item.rating,
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

   /* suspend fun updateItemCartToDb(
        item: Item,
        itemQty: Int
    ) {
        var taxPrice: Double = 0.0
        var cartItemPrice:Double = 0.0
        if (item.offerPrice > 0){
            cartItemPrice = item.cartPrice
            taxPrice = item.taxAmount
        }else{
            cartItemPrice = item.cartPrice
            taxPrice = item.taxAmount
        }
        cartItemPrice = itemQty * cartItemPrice
        taxPrice = itemQty * taxPrice
        var dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        var repository = ItemRepository(dao)
        repository.updateItemWithParameter(itemQty,cartItemPrice,taxPrice,item.productId)

        var taxDao : TaxItemDAO = GroceerDatabase.getInstance(this).taxItemDAO
        var taxRepository = TaxItemRepository(taxDao)
        var taxItemDbList: List<TaxItem> = taxRepository.getTaxItemWithProductId(item.productId)
        for (taxItem in taxItemDbList){
            taxItem.id?.let { taxRepository.updateTaxWithId(taxPrice, it) }
        }
    }
*/

    suspend fun getAllCartItem(productId: Int):List<Item>{
        val dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        val respository = ItemRepository(dao)
        var item:List<Item> = respository.getItemWithId(productId)
        return item
    }

    suspend fun getAllTax(productParentId: Int): List<Tax> {
        val dao: TaxDAO = GroceerDatabase.getInstance(this).taxDAO
        val respository = TaxRepository(dao)
        var tax:List<Tax> = respository.getTaxWithProductParentId(productParentId)

        return tax
    }

    suspend fun deleteItemFromCart(productId:Int){
        CoroutineScope(Dispatchers.Main).launch {
            var dao: ItemDAO = GroceerDatabase.getInstance(applicationContext).itemDAO
            var repository = ItemRepository(dao)
            repository.deleteById(productId)

            var taxDao : TaxItemDAO = GroceerDatabase.getInstance(applicationContext).taxItemDAO
            var taxRepository = TaxItemRepository(taxDao)
            taxRepository.deleteTaxById(productId)
        }
    }
}