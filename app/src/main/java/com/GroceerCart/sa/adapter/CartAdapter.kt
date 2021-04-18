package com.GroceerCart.sa.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.cart.ItemDAO
import com.GroceerCart.sa.db.cart.ItemRepository
import com.GroceerCart.sa.db.db.cart.Item
import com.GroceerCart.sa.filter.GroceerFilterProductListActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.cart_item_list.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CartAdapter(
    activity: Context,
    item: List<Item> , mListener:cartItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var context: Context = activity
    private var itemList : List<Item> = item
    private lateinit var  sharedPreferences: SharedPreferences
    private val sharedPrefFile = "groceerpreference"
    private var itemQty:Int = 0
    private var listener = mListener
    private lateinit var itemListDb:List<Item>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item_list, parent, false)
        sharedPreferences = context?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val priceSymbol: String? = sharedPreferences.getString("priceSymbol","")
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        context?.applicationContext?.let {
            Glide.with(it).load( itemList.get(position).imagePath)
                .apply(requestOptions).into(viewHolder.itemView.cartItemImage)
        }
        viewHolder.itemView.tvCartTitle.setText(itemList.get(position).productName)
        viewHolder.itemView.ratingCart.rating = itemList.get(position).rating.toFloat()
        viewHolder.itemView.tvCartOfferPrice.setText(priceSymbol +  itemList.get(position).offerPrice.toString())
        viewHolder.itemView.tvCartActualPrice.setText(priceSymbol + itemList.get(position).sellingPrice.toString())

        itemQty = itemList.get(position).cartItem
        viewHolder.itemView.tvCartQty.setText(itemQty.toString())
       /* CoroutineScope(Dispatchers.Main).launch {
            itemListDb = getAllCartItem(itemList.get(position).productId)
            if (itemListDb!= null && itemListDb.size >0){
                itemQty = itemListDb.get(position).cartItem
                viewHolder.itemView.tvCartQty.setText(itemQty.toString())
            }else{
                itemQty = 0
            }
            Log.e("Cart Add","during loading - " + itemQty)
        }*/

     /*   itemQty = itemList.get(position).cartItem
        viewHolder.itemView.tvCartQty.setText(itemQty.toString())*/

        viewHolder.itemView.btnCartQtyAdd.setOnClickListener {

            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(itemList.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(0).cartItem + 1
                    viewHolder.itemView.tvCartQty.setText(itemQty.toString())
                    Log.e("Cart qty Add","cart add more than  cart itemm - " + itemQty)
                    listener.updateCartItemToDb(position,itemQty )
                }else{
                    itemQty = 1
                    viewHolder.itemView.tvCartQty.setText(itemQty.toString())
                    Log.e("Cart qty  Add","cart add one  cart itemm" + itemQty)
                    listener.updateCartItemToDb(position,itemQty )
                }
            }


        }

        viewHolder.itemView.btnCartQtyMinus.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                itemListDb = getAllCartItem(itemList.get(position).productId)
                if (itemListDb!= null && itemListDb.size >0){
                    itemQty = itemListDb.get(0).cartItem
                    if (itemQty > 1){
                        itemQty -= 1
                        viewHolder.itemView.tvCartQty.setText(itemQty.toString())
                        listener.updateCartItemToDb(position,itemQty)
                    }
                }
            }

        }

        viewHolder.itemView.ivCartDelete.setOnClickListener {
            listener.deleteCartItemDb(position)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemTitle: TextView
        var ratingBar :RatingBar
        var cartItemImage: ImageView
        var cartOfferPrice:TextView
        var cartActualPrice :TextView
        var cartItemDelete: ImageView
        var cartAddButton : Button
        var cartMinusButton : Button
        var cartItemQty : TextView
        init {
            itemTitle = itemView.findViewById(R.id.tvCartTitle)
            ratingBar = itemView.findViewById(R.id.ratingCart)
            cartItemImage = itemView.findViewById(R.id.cartItemImage)
            cartOfferPrice = itemView.findViewById(R.id.tvCartOfferPrice)
            cartActualPrice = itemView.findViewById(R.id.tvCartActualPrice)
            cartItemDelete = itemView.findViewById(R.id.ivCartDelete)
            cartAddButton = itemView.findViewById(R.id.btnCartQtyAdd)
            cartMinusButton = itemView.findViewById(R.id.btnCartQtyMinus)
            cartItemQty = itemView.findViewById(R.id.tvCartQty)

        }
    }

    interface cartItemListener{
        fun updateCartItemToDb(position: Int, itemQty: Int)
        fun deleteCartItemDb(position: Int)
    }
    suspend fun getAllCartItem(productId: Int):List<Item>{
        val dao: ItemDAO = GroceerDatabase.getInstance(context).itemDAO
        val respository = ItemRepository(dao)
        var item:List<Item> = respository.getItemWithId(productId)
        return item
    }
}