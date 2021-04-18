package com.GroceerCart.sa.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GorceerCategoryActivity
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.service.homeservice.Table
import com.GroceerCart.sa.service.homeservice.Table1
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.top_slider_item.view.*


class HomeBannerAdapter(private val context: FragmentActivity, table: List<Table1>) : PagerAdapter() {
    private  val MAX_VALUE = 200
    private var bannerList : List<Table1> = table
    private var inflater: LayoutInflater? = null
    private val images = arrayOf(R.mipmap.groceer_logo, R.mipmap.groceer_logo, R.mipmap.groceer_logo)

    override fun isViewFromObject(view: View, `object`: Any): Boolean {

        return view === `object`
    }

    override fun getCount(): Int {

        return bannerList.size
        //return 2
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater!!.inflate(R.layout.top_slider_item, null)
        var tvTitle = view.findViewById<TextView>(R.id.textSliderTitle3)
        var tvSubTitle = view.findViewById<TextView>(R.id.textSliderSubTitle)
        var tvTitleSub = view.findViewById<TextView>(R.id.textSliderTitle)
        var cardViewBanner = view.findViewById<CardView>(R.id.bannerCardView)

        tvTitle.text = bannerList.get(position).title.toString()
        tvSubTitle.text = bannerList.get(position).title1.toString()
        tvTitleSub.text = bannerList.get(position).title2.toString()

        cardViewBanner.setOnClickListener {
            context.startActivity(Intent(context, GorceerCategoryActivity::class.java))
        }

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(context).load(/*GroceerApiInstance.BASE_URL1 +*/ bannerList.get(position).imagePath)
            .apply(requestOptions).into(view.imgView_slide)
        val vp = container as ViewPager
        vp.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

        val vp = container as ViewPager
        val view = `object` as View
        vp.removeView(view)
    }


}