package com.GroceerCart.sa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.ui.home.HomeFragment
import com.GroceerCart.sa.api.GroceerApiInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class HomeGridAdapter(private val context: Context) : BaseAdapter() {
    var gridImageList  = arrayOf(R.mipmap.shipping,R.mipmap.service,R.mipmap.easy_return,R.mipmap.online_payment)
    var gridText = arrayOf("FREE SHIPPING","24/7 SERVICE","EASY RETURNS","ONLINE PAYMENTS")
    override fun getView(position: Int, p1: View?, parent: ViewGroup?): View {
// Inflate the custom view
        val inflater = parent?.context?.
        getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.home_grid_list_item,null)

        // Get the custom view widgets reference
        val tvGrid = view.findViewById<TextView>(R.id.gridText)
        val imgGrid = view.findViewById<ImageView>(R.id.gridImage)
        imgGrid.setBackgroundResource(gridImageList[position])

        // Display color name on text view
        tvGrid.text = gridText[position]

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        /*Glide.with(context).load(GroceerApiInstance.BASE_URL1 + freshList.get(position).imagePath)
            .apply(requestOptions).into(R.id.imgage)*/

      /*  // Set a click listener for card view
        card.setOnClickListener{
            // Show selected color in a toast message
            Toast.makeText(parent.context,
                "Clicked : }" + vendorList.get(position).clientName, Toast.LENGTH_SHORT).show()

            // Get the activity reference from parent
            val activity  = parent.context as Activity

            // Get the activity root view
            val viewGroup = activity.findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0)


        }*/

        // Finally, return the view
        return view    }

    override fun getItem(p0: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getItemId(p0: Int): Long {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        return 4
    }
}