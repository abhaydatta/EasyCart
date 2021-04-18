package com.GroceerCart.sa.activity.ui.vendor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.liveData
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.adapter.VendorListAdapter
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.service.Table
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.Vendors
import kotlinx.android.synthetic.main.content_vendor_list.*
import org.json.JSONObject
import retrofit2.Response


class VendorFragment : Fragment() {

    private lateinit var vendorViewModel: VendorViewModel
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var searchVendor :SearchView
    private lateinit var vendorAdapter:VendorListAdapter
    private lateinit var progressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vendorViewModel =
            ViewModelProviders.of(this).get(VendorViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        var activity : GroceerHomeActivity = context as GroceerHomeActivity

        groceerApiService = GroceerApiInstance.getRetrofitInstance(activity.vendor_code).create(GroceerApiService::class.java)

        searchVendor = root.findViewById(R.id.searchView)

        progressBar = root.findViewById(R.id.vendorProgressBar)
        //  searchVendor = root.findViewById(R.id.searchView)
        if (activity.getConnectionStatus()){
            progressBar.visibility = View.VISIBLE
            getVendorList()
        }else{
            Toast.makeText(getActivity(),"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }


        searchVendor.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                 return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                vendorAdapter.filter.filter(newText)
                return false
            }

        })
        return root
    }

    private fun getVendorList() {
        Log.e("API" ,"Fucntin call")
        val responseLiveData : LiveData<Response<Vendors>> = liveData {
            val rootObject= JSONObject()
            rootObject.put("seqno",1)
            rootObject.put("searchCode","500032")
            rootObject.put("condition","CHECKPIN")

            Log.e("String",rootObject.toString())

            val response = groceerApiService.postRawJSON(rootObject.toString())

            //val response = vendorService.getVendors(1,"500032","CHECKPIN")
            emit(response)
        }

        responseLiveData.observe(viewLifecycleOwner, Observer {
            val table =it?.body()
            if (table!= null){
                progressBar.visibility = View.GONE
                if (table.status.equals("200")){
                    val table: List<Table> = table.objresult.table
                    initAdapter(table)
                }else{
                    progressBar.visibility = View.GONE
                    Toast.makeText(activity,table.message.toString(),Toast.LENGTH_LONG).show()
                }

            }
        })
    }

    private fun initAdapter(table: List<Table>) {
           vendorAdapter = activity?.let { VendorListAdapter(it,table) }!!
        //vendorAdapter = this?.let { activity?.let { it1 -> VendorListAdapter(it1,table) } }!!
        vendorGridView.adapter = vendorAdapter
        progressBar.visibility = View.GONE
        vendorGridView.visibility  = View.VISIBLE

    }

}