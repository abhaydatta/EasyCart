package com.GroceerCart.sa.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.activity.GroceerProductDetailActivity
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
import com.GroceerCart.sa.service.homeservice.Table3
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.home_trending_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

class TrendingProductAdapter(
    private val context: GroceerHomeActivity,
    productList: List<Table3>,
    priceSymmbol: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"
    private var itemQty:Int = 0
    private lateinit var itemListDb:List<Item>
    private  var productList: List<Table3> = productList
    private var priceSymmbol:String = priceSymmbol
    private var vendorId:Int = 0
    private var itemPrice:Double = 0.0

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.home_trending_item, viewGroup, false)
        sharedPreferences = context?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        val gson = Gson()
        val json: String? = sharedPreferences.getString("vendorObject", "")
        val vendorDetail: JSONObject = gson.fromJson(json, JSONObject::class.java)
        vendorId   = vendorDetail.getInt("seqno")
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        viewHolder.itemView.tvProductTitle.text = productList.get(position).name
        var offerPrice : Double = productList.get(position).offerPrice
        if (offerPrice > 0){
            viewHolder.itemView.tvProductActualPrice.visibility = View.VISIBLE
            viewHolder.itemView.tvProductOfferPrice.visibility = View.VISIBLE
            viewHolder.itemView.crossViewTrending.visibility = View.VISIBLE
            viewHolder.itemView.tvProductActualPrice.text = priceSymmbol + productList.get(position).sellingPrice.toString()
            viewHolder.itemView.tvProductOfferPrice.text = priceSymmbol + productList.get(position).offerPrice
        }else{
            viewHolder.itemView.tvProductActualPrice.visibility = View.VISIBLE
            viewHolder.itemView.tvProductOfferPrice.visibility = View.VISIBLE
            viewHolder.itemView.crossViewTrending.visibility = View.VISIBLE
            viewHolder.itemView.tvProductOfferPrice.text = priceSymmbol + productList.get(position).sellingPrice.toString()
            viewHolder.itemView.tvProductActualPrice.text = priceSymmbol + productList.get(position).offerPrice.toString()

        }

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(context).load( /*GroceerApiInstance.BASE_URL1 + */productList.get(position).imagePath)
            .apply(requestOptions).into(viewHolder.itemView.imgProduct)

        viewHolder.itemView.linearLayoutTrending.setOnClickListener {
            context.startActivity(Intent(context, GroceerProductDetailActivity::class.java).putExtra("productId",productList.get(position).productId))
        }


        CoroutineScope(Dispatchers.Main).launch {
            itemListDb = getAllCartItem(productList.get(position).productId)
            if (itemListDb!= null && itemListDb.size >0){
                viewHolder.itemView.btnAddToCart.visibility = View.GONE
                viewHolder.itemView.cartAddView.visibility = View.VISIBLE
                itemQty = itemListDb.get(0).cartItem
                viewHolder.itemView.edtQty.setText(itemQty.toString())
            }else{
                itemQty = 0
            }
            Log.e("Cart Add","during loading - " + itemQty)

        }

        viewHolder.itemView.btnQtyAdd.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(productList.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(0).cartItem + 1
                    viewHolder.itemView.edtQty.setText(itemQty.toString())
                    Log.e("Cart qty Add","cart add more than  cart itemm - " + itemQty)
                    updateItemToDb(position,itemQty )
                }else{
                    itemQty = 1
                    viewHolder.itemView.edtQty.setText(itemQty.toString())
                    Log.e("Cart qty  Add","cart add one  cart itemm" + itemQty)
                    updateItemToDb(position,itemQty )
                }
            }

        }

        viewHolder.itemView.btnQtyMinus.setOnClickListener {

            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(productList.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(0).cartItem
                    if (itemQty == 1){
                        viewHolder.itemView.btnAddToCart.visibility = View.VISIBLE
                        viewHolder.itemView.cartAddView.visibility = View.GONE
                        // delete item from cart
                        deleteItemFromCart(productList.get(position).productId)
                    }else{
                        itemQty -= 1
                        viewHolder.itemView.edtQty.setText(itemQty.toString())
                        updateItemToDb(position,itemQty)
                    }

                }
            }

        }

        viewHolder.itemView.btnAddToCart.setOnClickListener {
            var loginMode:Boolean = sharedPreferences.getBoolean("login_mode",false)
            if (loginMode){
                itemQty = 0
                viewHolder.itemView.btnAddToCart.visibility = View.GONE
                viewHolder.itemView.cartAddView.visibility = View.VISIBLE
                itemQty += 1
                viewHolder.itemView.edtQty.setText(itemQty.toString())
                Log.e("Cart btn  Add","cart add more than  cart itemm - " + itemQty)
                insertItemToDb(position,itemQty)

            }else{
                Toast.makeText(context,"Please Login first before adding item to the cart !" , Toast.LENGTH_LONG).show()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var productItemImage: ImageView
        var productItemTitle: TextView
        var productOfferPrice: TextView
        var productActualPrice: TextView
        var btnOfferText : Button
        var btnOfferDiscount : Button
        var btnAddToCart : Button
        var linearTrending: LinearLayout
        var crossView : View
        val cartView : RelativeLayout
        val cartItemQtyAdd: Button
        val cartItemQtyRemove : Button
        val edtItemQty : EditText

        init {
            productItemImage = itemView.findViewById(R.id.imgProduct)
            productItemTitle = itemView.findViewById(R.id.tvProductTitle)
            productOfferPrice = itemView.findViewById(R.id.tvProductOfferPrice)
            productActualPrice = itemView.findViewById(R.id.tvProductActualPrice)
            btnOfferText = itemView.findViewById(R.id.btnOfferText)
            btnOfferDiscount = itemView.findViewById(R.id.btnItemDiscount)
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart)
            linearTrending = itemView.findViewById(R.id.linearLayoutTrending)
            crossView = itemView.findViewById(R.id.crossViewTrending)
            cartView = itemView.findViewById(R.id.cartAddView)
            cartItemQtyAdd = itemView.findViewById(R.id.btnQtyAdd)
            cartItemQtyRemove = itemView.findViewById(R.id.btnQtyMinus)
            edtItemQty = itemView.findViewById(R.id.edtQty)

        }
    }

    suspend fun getAllCartItem(productId: Int):List<Item>{
        val dao: ItemDAO = GroceerDatabase.getInstance(context).itemDAO
        val respository = ItemRepository(dao)
        var item:List<Item> = respository.getItemWithId(productId)
        return item
    }

    suspend fun deleteItemFromCart(productId:Int){
        CoroutineScope(Dispatchers.Main).launch {
            var dao: ItemDAO = GroceerDatabase.getInstance(context).itemDAO
            var repository = ItemRepository(dao)
            repository.deleteById(productId)

            var taxDao : TaxItemDAO = GroceerDatabase.getInstance(context).taxItemDAO
            var taxRepository = TaxItemRepository(taxDao)
            taxRepository.deleteTaxById(productId)
        }
    }

     fun insertItemToDb(position: Int, itemQty: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            var listTax: List<Tax> = getAllTax(productList.get(position).products_parentId)
            addItemCartToDb(productList.get(position),itemQty,listTax)
        }
    }

     fun updateItemToDb(position: Int, itemQty: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            var listTax: List<Tax> = getAllTax(productList.get(position).products_parentId)
            //var productItem : List<Item> = getAllCartItem(productList.get(position).productId)
            updateItemCartToDb(productList.get(0), itemQty,listTax)
        }
    }

    suspend fun addItemCartToDb(
        item: Table3,
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
            var taxDao : TaxItemDAO = GroceerDatabase.getInstance(context).taxItemDAO
            var taxRepository = TaxItemRepository(taxDao)
            taxRepository.insertTaxItem(
                TaxItem(
                null,item.productId,vendorId,tax.seqno,tax.name,tax.taxType,tax.taxValue,taxItemPrice,tax.groupId)
            )
        }
        cartItemPrice *= itemQty
        cartItemPrice = BigDecimal(cartItemPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        taxPrice *= itemQty
        taxPrice = BigDecimal(taxPrice).setScale(2, RoundingMode.HALF_UP).toDouble()
        var dao: ItemDAO = GroceerDatabase.getInstance(context).itemDAO
        var repository = ItemRepository(dao)
        repository.insertItem(
            Item(null,item.productId,item.products_parentId,0,
                0,vendorId,1,item.name," ",
                " "," ", item.imagePath,item.cartStockInHand,0,
                item.offerPrice,item.sellingPrice,
                itemPrice,itemQty,cartItemPrice,taxPrice,0.0)
        )
    }
    
    private suspend fun updateItemCartToDb(
        item: Table3,
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
        var dao: ItemDAO = GroceerDatabase.getInstance(context).itemDAO
        var repository = ItemRepository(dao)
        repository.updateItemWithParameter(itemQty,cartItemPrice,taxPrice,item.productId)

        var taxItemPriceNew: Double = 0.0
        var taxDao: TaxItemDAO = GroceerDatabase.getInstance(context).taxItemDAO
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
        val dao: TaxDAO = GroceerDatabase.getInstance(context).taxDAO
        val respository = TaxRepository(dao)
        var tax:List<Tax> = respository.getTaxWithProductParentId(productParentId)
        return tax
    }

}