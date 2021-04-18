package com.GroceerCart.sa.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.adapter.CountryAdapter
import com.GroceerCart.sa.service.country.Table

class CountryDialogFragment(countryList: List<Table>, listener:onDialogClick) : DialogFragment(),CountryAdapter.onItemClickListener {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var  countryRecyclerView : RecyclerView
    private var mCountryList: List<Table> = countryList
    private var mListener: onDialogClick = listener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView : View = inflater.inflate(R.layout.layout_country_dialog,container,false)
        countryRecyclerView = rootView.findViewById(R.id.country_recyclerView)
        layoutManager = LinearLayoutManager(activity)
        countryRecyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        countryRecyclerView.setHasFixedSize(true)
        countryRecyclerView.layoutManager = layoutManager

        adapter = CountryAdapter(activity,mCountryList,this)
        countryRecyclerView.adapter = adapter

        return rootView
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT)
    }

    override fun onItemClick(position: Int) {
        mListener.dialogClick(position)
        dismiss()
    }

    interface onDialogClick{
        fun dialogClick(position: Int)
    }
}