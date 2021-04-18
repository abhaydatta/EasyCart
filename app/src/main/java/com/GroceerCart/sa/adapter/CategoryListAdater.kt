package com.GroceerCart.sa.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.service.categoryservice.Objresult
import kotlinx.android.synthetic.main.category_list_item.view.*

class CategoryListAdater(private val context: Context, categoryList: List<Objresult>)  : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private  var categoryList: List<Objresult> = categoryList
    private var  mContext: Context = context
    private var viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CategoryListAdater.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.category_list_item, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        Log.e("Category",  "" + categoryList.size)
        return categoryList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       // holder.itemView.tag = position
       // holder.itemView.setOnClickListener(clickListener)
        holder.itemView.categoryTitle.text = categoryList.get(position).name
        val childLayoutManager = GridLayoutManager(holder.itemView.categoryGridView.context, 2)

        holder.itemView.categoryGridView.apply {
            layoutManager = childLayoutManager
            adapter = CategoryAdapter(mContext,categoryList.get(position).subcategories)
            setRecycledViewPool(viewPool)
        }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var categoryTitle: Button
        var categoryGrid: RecyclerView

        init {
            categoryTitle = itemView.findViewById(R.id.categoryTitle)
            categoryGrid = itemView.findViewById(R.id.categoryGridView)
        }
    }
}