package com.GroceerCart.sa.activity.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.activity.GroceerOrderListActivity
import com.GroceerCart.sa.adapter.CartAdapter
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
import kotlinx.android.synthetic.main.fragment_cart.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroceerCartFragment : Fragment(), CartAdapter.cartItemListener {
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private var layoutManager: RecyclerView.LayoutManager? = null
    lateinit var activity: GroceerHomeActivity
    lateinit var itemList: List<Item>
    private var taxPrice: Double = 0.0
    private var cartItemPrice: Double = 0.0
    private lateinit var tvEmptyCart: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_cart, container, false)
        activity = context as GroceerHomeActivity
        layoutManager = LinearLayoutManager(activity)

        tvEmptyCart = root.findViewById(R.id.tvEmptyCart)

        cartRecyclerView = root.findViewById(R.id.cartRecyclerView)
        cartRecyclerView.addItemDecoration(
            DividerItemDecoration(
                activity,
                LinearLayoutManager.VERTICAL
            )
        )
        cartRecyclerView.setHasFixedSize(true)
        cartRecyclerView.layoutManager = layoutManager

        CoroutineScope(Dispatchers.Main).launch {
            llProgressBar.visibility = View.VISIBLE
            getAllCartItem()
        }

        var btnCartSubmit = root.findViewById<Button>(R.id.btnCartSubmit)
        btnCartSubmit?.setOnClickListener {
            startActivity(Intent(activity, GroceerOrderListActivity::class.java))
        }

        return root
    }

    suspend fun getAllCartItem() {
        val dao: ItemDAO = GroceerDatabase.getInstance(requireActivity().applicationContext).itemDAO
        val respository = ItemRepository(dao)
        itemList = respository.getAllItem()
        withContext(Dispatchers.Main) {
            loadAdapter(itemList)
        }

    }

    private fun loadAdapter(item: List<Item>) {
        adapter = CartAdapter(activity, item, this)
        cartRecyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        if (itemList.size > 0) {
            tvEmptyCart.visibility = View.GONE
            btnCartSubmit.visibility = View.VISIBLE

        } else {
            tvEmptyCart.visibility = View.VISIBLE
            btnCartSubmit.visibility = View.GONE
        }
        llProgressBar.visibility = View.GONE
    }

    override fun updateCartItemToDb(position: Int, itemQty: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            var listTax: List<Tax> = getAllTax(itemList.get(position).productsParentId)
            updateItemCartToDb(itemList.get(position), itemQty, listTax)
        }
    }


    override fun deleteCartItemDb(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            var dao: ItemDAO = GroceerDatabase.getInstance(activity).itemDAO
            var repository = ItemRepository(dao)
            repository.deleteById(itemList.get(position).productId)

            var taxDao: TaxItemDAO = GroceerDatabase.getInstance(activity).taxItemDAO
            var taxRepository = TaxItemRepository(taxDao)
            taxRepository.deleteTaxById(itemList.get(position).productId)
            getAllCartItem()
        }

    }

    suspend fun updateItemCartToDb(
        item: Item,
        itemQty: Int,
        listTax: List<Tax>
    ) {
        var taxPrice: Double = 0.0
        var taxItemPrice: Double = 0.0
        var cartItemPrice: Double = 0.0
        if (item.offerPrice > 0) {
            cartItemPrice = item.offerPrice
            // taxPrice = item.taxAmount
        } else {
            cartItemPrice = item.sellingPrice
            // taxPrice = item.taxAmount
        }
        for (tax in listTax) {
            if (tax.taxType.equals("Percentage")) {
                if (item.offerPrice > 0) {
                    taxItemPrice = (item.offerPrice / 100) * tax.taxValue
                    taxPrice += taxItemPrice
                } else {
                    taxItemPrice = (item.sellingPrice / 100) * tax.taxValue
                    taxPrice += taxItemPrice
                }
            } else {
                taxItemPrice = tax.taxValue
                taxPrice += taxItemPrice
            }
        }

        cartItemPrice *= itemQty
        taxPrice *= itemQty

        var dao: ItemDAO = GroceerDatabase.getInstance(activity).itemDAO
        var repository = ItemRepository(dao)
        repository.updateItemWithParameter(itemQty, cartItemPrice, taxPrice, item.productId)

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
        var tax: List<Tax> = respository.getTaxWithProductParentId(productParentId)

        return tax
    }

}