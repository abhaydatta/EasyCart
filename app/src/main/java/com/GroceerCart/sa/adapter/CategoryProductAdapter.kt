package com.GroceerCart.sa.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerProductDetailActivity
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.cart.ItemDAO
import com.GroceerCart.sa.db.cart.ItemRepository
import com.GroceerCart.sa.db.db.cart.Item
import com.GroceerCart.sa.db.taxitem.TaxItemDAO
import com.GroceerCart.sa.db.taxitem.TaxItemRepository
import com.GroceerCart.sa.service.productitem.Table
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryProductAdapter(mcontext: Context, produuctListResponse: List<Table>,listener:productListener, chkQty:Boolean) : BaseAdapter() {
    var context:Context = mcontext
    var productListResponse : List<Table> = produuctListResponse
    val mListener:productListener = listener
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"
    private var itemQty:Int = 0
    private lateinit var itemListDb:List<Item>
    private  var checkQty:Boolean = chkQty
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Inflate the custom view
        val inflater = parent?.context?.
        getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.category_product_list_itemm,null)
        sharedPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        // Get the custom view widgets reference
        val tvProductTitle = view.findViewById<TextView>(R.id.tvCategoryProductTitle)
        val tvProductWeight = view.findViewById<Button>(R.id.tvProductWeight)
        val tvProductPrice = view.findViewById<TextView>(R.id.tvProductPrice)
        val imgItem = view.findViewById<ImageView>(R.id.ivProduct)
        val tvProductActualPrice = view.findViewById<TextView>(R.id.tvCatProductActualPrice)
        val tvProductOfferPrice = view.findViewById<TextView>(R.id.tvCatProductOfferPrice)
        val btnAddCart = view.findViewById<Button>(R.id.btnCatAddToCart)
        val viewCross = view.findViewById<View>(R.id.crossView)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingProduct)
        val top_offerText = view.findViewById<Button>(R.id.btnOfferText)
        val cartView = view.findViewById<RelativeLayout>(R.id.cartAddView)
        val cartItemQtyAdd = view.findViewById<Button>(R.id.btnQtyAdd)
        val cartItemQtyRemove = view.findViewById<Button>(R.id.btnQtyMinus)
        val edtItemQty = view.findViewById<EditText>(R.id.edtQty)
        val btnCartNotify = view.findViewById<Button>(R.id.btnCartNotify)

        if (productListResponse.get(position).cartStockInHand == 0 && checkQty) {
            btnAddCart.visibility = View.GONE
            cartView.visibility = View.GONE
            btnCartNotify.visibility = View.VISIBLE
        }else{
            btnCartNotify.visibility = View.GONE
            btnAddCart.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(productListResponse.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    btnAddCart.visibility = View.GONE
                    cartView.visibility = View.VISIBLE
                    itemQty = itemListDb.get(0).cartItem
                    edtItemQty.setText(itemQty.toString())
                }else{
                    itemQty = 0
                }
                Log.e("Cart Add","during loading - " + itemQty)
            }
        }

        cartItemQtyAdd.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(productListResponse.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(0).cartItem + 1
                    edtItemQty.setText(itemQty.toString())
                    Log.e("Cart qty Add","cart add more than  cart itemm - " + itemQty)
                    mListener.updateItemToDb(position,itemQty )
                }else{
                    itemQty = 1
                    edtItemQty.setText(itemQty.toString())
                    Log.e("Cart qty  Add","cart add one  cart itemm" + itemQty)
                    mListener.updateItemToDb(position,itemQty )
                }
            }

        }

        cartItemQtyRemove.setOnClickListener {

            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(productListResponse.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(0).cartItem
                    if (itemQty == 1){
                        btnAddCart.visibility = View.VISIBLE
                        cartView.visibility = View.GONE
                        // delete item from cart
                        deleteItemFromCart(productListResponse.get(position).productId)
                    }else{
                        itemQty -= 1
                        edtItemQty.setText(itemQty.toString())
                        mListener.updateItemToDb(position,itemQty)
                    }

                }
            }

        }

        btnAddCart.setOnClickListener {
            var loginMode:Boolean = sharedPreferences.getBoolean("login_mode",false)
            if (loginMode){
                itemQty = 0
                btnAddCart.visibility = View.GONE
                cartView.visibility = View.VISIBLE
                itemQty += 1
                edtItemQty.setText(itemQty.toString())
                Log.e("Cart btn  Add","cart add more than  cart itemm - " + itemQty)
                mListener.insertItemToDb(position,itemQty)

            }else{
                Toast.makeText(context,"Please Login first before adding item to the cart !" , Toast.LENGTH_LONG).show()
            }
        }
        // Display color name on text view
        tvProductTitle.text = productListResponse.get(position).name
        ratingBar.rating = productListResponse.get(position).rating.toFloat()
        tvProductWeight.text = productListResponse.get(position).uomName
        tvProductPrice.text = productListResponse.get(position).sellingPrice.toString().trim()

        if (productListResponse.get(position).comboName.isNotBlank()){
            top_offerText.visibility = View.VISIBLE
            top_offerText.text = productListResponse.get(position).comboName
        }else{
            top_offerText.visibility = View.GONE
        }
        val priceSymbol: String? = sharedPreferences.getString("priceSymbol","")
        if (productListResponse.get(position).offerPrice > 0){
            tvProductOfferPrice.text =  priceSymbol + productListResponse.get(position).offerPrice.toString()
            tvProductActualPrice.text =  priceSymbol + productListResponse.get(position).sellingPrice.toString()
            viewCross.visibility = View.VISIBLE
        }else{
            //tvProductActualPrice.visibility = View.INVISIBLE
            tvProductActualPrice.text =  priceSymbol + productListResponse.get(position).offerPrice.toString()
            tvProductOfferPrice.text =  priceSymbol + productListResponse.get(position).sellingPrice.toString()
            viewCross.visibility = View.VISIBLE
        }

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(context).load( /*GroceerApiInstance.BASE_URL1 +*/ productListResponse.get(position).imagePath)
            .apply(requestOptions).into(imgItem)


        val layout_linear: LinearLayout = view.findViewById(R.id.linearLayout3)
        layout_linear.setOnClickListener {
            var mIntent : Intent = Intent(context, GroceerProductDetailActivity::class.java)
            mIntent.putExtra("productId",productListResponse.get(position).productId)
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(mIntent)
        }

       /* val uomLayout : RelativeLayout = view.findViewById(R.id.linear_dropdown)
        uomLayout.setOnClickListener {
            Toast.makeText(context,"CLick",Toast.LENGTH_LONG).show()
        }
*/
        tvProductWeight.setOnClickListener {
            mListener.uomOpenDialog(position)
        }

        tvProductPrice.setOnClickListener {
            mListener.uomOpenDialog(position)
        }

        btnCartNotify.setOnClickListener {
            mListener.notifyProduct(position)
        }

        return view
    }

    override fun getItem(position: Int): Any {
       return  productListResponse.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return productListResponse.size
    }
    
    interface productListener{
        fun insertItemToDb(position: Int, itemQty: Int)
        fun updateItemToDb(position: Int, itemQty: Int)
        fun uomOpenDialog(position: Int)
        fun notifyProduct(position: Int)
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
}