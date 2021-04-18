package com.GroceerCart.sa.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.cart.ItemDAO
import com.GroceerCart.sa.db.cart.ItemRepository
import com.GroceerCart.sa.db.db.cart.Item
import com.GroceerCart.sa.db.taxitem.TaxItemDAO
import com.GroceerCart.sa.db.taxitem.TaxItemRepository
import com.GroceerCart.sa.service.productitem.UomProducts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.cart_item_list.view.*
import kotlinx.android.synthetic.main.layout_uom_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UomAdapter(
    activity: Context,
    mUomList: List<UomProducts>,
    checkQantity: Boolean,
    listener: UomAdapter.UomListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mListener  = listener
    private var uomList:List<UomProducts> = mUomList
    private var context: Context = activity
    private var checkQantity:Boolean = checkQantity
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"
    private var itemQty:Int = 0
    private lateinit var itemListDb:List<Item>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_uom_item, parent, false)
        sharedPreferences = context?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return uomList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.tvUomTitle.setText(uomList.get(position).name)
        val priceSymbol: String? = sharedPreferences.getString("priceSymbol","")
        if (uomList.get(position).offerPrice > 0){
            viewHolder.itemView.tvUoOfferPrice.setText(priceSymbol + uomList.get(position).offerPrice.toString())
            viewHolder.itemView.tvUoActualPrice.setText(  priceSymbol + uomList.get(position).sellingPrice.toString())
            viewHolder.itemView.uomCross.visibility = View.VISIBLE
        }else{
            //tvProductActualPrice.visibility = View.INVISIBLE
            viewHolder.itemView.tvUoActualPrice.setText(priceSymbol + uomList.get(position).offerPrice.toString())
            viewHolder.itemView.tvUoOfferPrice.setText(priceSymbol + uomList.get(position).sellingPrice.toString())
            //tvProductOfferPrice.text =  priceSymbol + uomList.get(position).sellingPrice.toString()
            viewHolder.itemView.uomCross.visibility = View.GONE
        }

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        context?.let {
            Glide.with(it).load( /*GroceerApiInstance.BASE_URL1 +*/ uomList.get(position).imagePath)
                .apply(requestOptions).into(viewHolder.itemView.uomItemImage)
        }

        if (uomList.get(position).cartStockInHand == 0 && checkQantity){
            viewHolder.itemView.btnUoNotify.visibility = View.VISIBLE
            viewHolder.itemView.btnUomAddToCart.visibility = View.GONE
            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(uomList.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(position).cartItem
                    viewHolder.itemView.tvCartQty.setText(itemQty.toString())
                }else{
                    itemQty = 0
                }
                Log.e("Cart Add","during loading - " + itemQty)
            }
        }else{
            viewHolder.itemView.btnUoNotify.visibility = View.GONE
            viewHolder.itemView.btnUomAddToCart.visibility = View.VISIBLE
        }

        viewHolder.itemView.btnUomAddToCart.setOnClickListener {
            var loginMode:Boolean = sharedPreferences.getBoolean("login_mode",false)
            if (loginMode){
                itemQty = 0
                viewHolder.itemView.btnUomAddToCart.visibility = View.GONE
                viewHolder.itemView.uomAddView.visibility = View.VISIBLE
                itemQty += 1
                viewHolder.itemView.edtQty.setText(itemQty.toString())
                Log.e("Cart btn  Add","cart add more than  cart itemm - " + itemQty)
                mListener.addItemCartUom(position,itemQty)

            }else{
                Toast.makeText(context,"Please Login first before adding item to the cart !" , Toast.LENGTH_LONG).show()
            }
        }

        viewHolder.itemView.btnQtyMinus.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(uomList.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(0).cartItem
                    if (itemQty == 1){
                        viewHolder.itemView.btnUomAddToCart.visibility = View.VISIBLE
                        viewHolder.itemView.uomAddView.visibility = View.GONE
                        // delete item from cart
                        deleteItemFromCart(uomList.get(position).productId)
                    }else{
                        itemQty -= 1
                        viewHolder.itemView.edtQty.setText(itemQty.toString())
                        mListener.updateItemCartUom(position,itemQty)
                    }

                }
            }
        }

        viewHolder.itemView.btnQtyAdd.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(uomList.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(0).cartItem + 1
                    viewHolder.itemView.edtQty.setText(itemQty.toString())
                    Log.e("Cart qty Add","cart add more than  cart itemm - " + itemQty)
                    mListener.updateItemCartUom(position,itemQty )
                }else{
                    itemQty = 1
                    viewHolder.itemView.edtQty.setText(itemQty.toString())
                    Log.e("Cart qty  Add","cart add one  cart itemm" + itemQty)
                    mListener.updateItemCartUom(position,itemQty )
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var uomTitle: TextView
        var uomItemImage: ImageView
        var uomOfferPrice: TextView
        var uomActualPrice : TextView
        var uomNotifyButton : Button
        var uomAddCartButton : Button
        var uomMinusButton : Button
        var uomAddQtyButton : Button
        var uomItemQty : EditText
        var uomCross :View
        var uomAddView : RelativeLayout
        init {
            uomTitle = itemView.findViewById(R.id.tvUomTitle)
            uomItemImage = itemView.findViewById(R.id.uomItemImage)
            uomOfferPrice = itemView.findViewById(R.id.tvUoOfferPrice)
            uomActualPrice = itemView.findViewById(R.id.tvUoActualPrice)
            uomNotifyButton = itemView.findViewById(R.id.btnUoNotify)
            uomAddCartButton = itemView.findViewById(R.id.btnUomAddToCart)
            uomMinusButton = itemView.findViewById(R.id.btnQtyMinus)
            uomAddQtyButton = itemView.findViewById(R.id.btnQtyAdd)
            uomItemQty = itemView.findViewById(R.id.edtQty)
            uomCross = itemView.findViewById(R.id.uomCross)
            uomAddView = itemView.findViewById(R.id.uomAddView)
        }
    }

    interface UomListener{
        fun addItemCartUom( position: Int, itemQty:Int)
        fun updateItemCartUom(position: Int, itemQty: Int)
        fun notifyUom(position: Int)
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