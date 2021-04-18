package com.GroceerCart.sa.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.service.homeservice.Table
import com.GroceerCart.sa.service.homeservice.Table6
import com.GroceerCart.sa.service.homeservice.Table8
import com.GroceerCart.sa.ui.CircularImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions


class ExpandableMenuListAdapter(activity: GroceerHomeActivity, headerList: List<Table>, childList: HashMap<Table, List<Table6>>) : BaseExpandableListAdapter() {
    private var activity:GroceerHomeActivity = activity
    private var headerList:List<Table> = headerList
    private var childList: HashMap<Table, List<Table6>> = childList

    override fun getGroup(headerPosition: Int): Any {
        return this.headerList[headerPosition]
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
         return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val headerTitle = getGroup(groupPosition) as Table
        if (convertView == null) {
            val infalInflater = activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.list_group_header, null)
        }
        val lblListHeader = convertView!!.findViewById(R.id.lblListHeader) as TextView
         val headerIcon = convertView!!.findViewById(R.id.itemImage) as CircularImageView
        //  Expandable View, Indicator right
        val headerIndicator = convertView.findViewById(R.id.iconimage) as ImageView
        lblListHeader.setTypeface(null, Typeface.NORMAL)
        lblListHeader.text = headerTitle.name


        //headerIcon.setImageResource(headerTitle.imagePath)
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(activity).load(/*GroceerApiInstance.BASE_URL1 +*/ headerTitle.imagePath)
            .apply(requestOptions).into(headerIcon)

       /* val eLV = parent as ExpandableListView
        eLV.expandGroup(groupPosition)*/

        // Expandable View, Indicator status
        if (getChildrenCount( groupPosition ) == 0) {
            headerIndicator.visibility = View.GONE
        } else {
            headerIndicator.visibility = View.VISIBLE
            if (isExpanded) {
                headerIndicator.setBackgroundResource((android.R.drawable.arrow_up_float))
            } else {
                headerIndicator.setBackgroundResource((android.R.drawable.arrow_down_float))
            }
        }
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        var childCount = 0
        childCount = this.childList[this.headerList[groupPosition]]!!.size
       /* if (groupPosition == 2 || groupPosition == 3) {
            childCount = this.childList[this.headerList[groupPosition]]!!.size
        }*/
        return childCount
    }

    override fun getChild(headerPosition: Int, childPosition: Int): Any {
       /* return this.listDataChild.get(this.listDataHeader.get(groupPosition))
            .get(childPosititon);*/
        return childList.get(headerList.get(headerPosition))?.get(childPosition)!!
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val childText = getChild(groupPosition, childPosition) as Table6

        if (convertView == null) {
            val infalInflater = this.activity

                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.list_group_child, null)
        }

        val txtListChild = convertView!!
            .findViewById(R.id.lblListItem) as TextView

        txtListChild.text = childText.name

        return convertView    }

    override fun getChildId(headerPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return headerList.size
    }
}