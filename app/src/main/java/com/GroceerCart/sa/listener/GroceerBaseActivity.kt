package com.GroceerCart.sa.listener

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.cart.ItemDAO
import com.GroceerCart.sa.db.cart.ItemRepository
import com.GroceerCart.sa.db.db.cart.Item
import com.GroceerCart.sa.db.vendor.Vendor
import com.GroceerCart.sa.db.vendor.VendorDAO
import com.GroceerCart.sa.db.vendor.VendorRepository
import com.GroceerCart.sa.service.GroceerApiService


open class GroceerBaseActivity : AppCompatActivity() {
    private lateinit var connectionStatus:String
    private lateinit var groceerApiService: GroceerApiService

  /*  lateinit var  table: List<Table>
    lateinit var  table1: List<Table1>
     lateinit var  table2: List<Table2>
     lateinit var  table3: List<Table3>
     lateinit var  table4: List<Table4>
     lateinit var  table5: List<Table5>
     lateinit var  table6: List<Table6>
     lateinit var  table7: List<Table7>
     lateinit var  table8: List<Table8>
     lateinit var  table9: List<Table9>*/


/*


    fun fetchGroceerHomeData(vendor_id:String){
        groceerApiService = GroceerApiInstance.getRetrofitInstance().create(GroceerApiService::class.java)

        val rootObject= JSONObject()
        rootObject.put("type",63)
        rootObject.put("userId",0)
        rootObject.put("filterId",2)
        rootObject.put("clientId","5f53c55a")
        rootObject.put("condition","")
        rootObject.put("clientSeqno",vendor_id)
        rootObject.put("searchName","")
        rootObject.put("locationId",0)
        rootObject.put("branchId",0)
        Log.e("String",rootObject.toString())

        val responseLiveData : LiveData<Response<HomeApi>> = liveData {

            val response = groceerApiService.getHomeDetail(rootObject.toString())
            emit(response)
        }

        responseLiveData.observe(this, Observer {
            val homeResponse = it?.body()
            if (homeResponse!= null){
               // prepareMenuData(homeResponse .objresult.table,homeResponse .objresult.table8)
                table = homeResponse.objresult.table
                table1 =homeResponse .objresult.table1
                table2 =homeResponse .objresult.table2
                table3=homeResponse .objresult.table3
                table4 =homeResponse .objresult.table4
                table5=homeResponse .objresult.table5
                table6=homeResponse .objresult.table6
                table7 =homeResponse .objresult.table7
                table8=homeResponse .objresult.table8
                table9 =homeResponse .objresult.table9

            }
        })
    }
*/



    /*fun getConnection():String{
       val cm: ConnectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
       val builder: NetworkRequest.Builder = NetworkRequest.Builder()

       cm.registerNetworkCallback(
           builder.build(),
           object : ConnectivityManager.NetworkCallback() {

               override fun onAvailable(network: Network) {
                   Log.i("MainActivity", "onAvailable!")
                    connectionStatus = "true"
                   // check if NetworkCapabilities has TRANSPORT_WIFI
                   val isWifi:Boolean = cm.getNetworkCapabilities(network)?.hasTransport(
                       NetworkCapabilities.TRANSPORT_WIFI) ?: true

                   // doSomething()
               }

               override fun onLost(network: Network) {
                   Log.i("MainActivity", "onLost!")
                   connectionStatus = "false"
                   // doSomething
               }
           }
       )
       return connectionStatus
   }*/

    fun getConnectionStatus():Boolean{
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

    open suspend fun getAllVendor():List<Vendor>{
        val dao: VendorDAO = GroceerDatabase.getInstance(this).vendorDAO
        val respository = VendorRepository(dao)
        var vendorList:List<Vendor> = respository.getAllVendor()
        return vendorList
    }

    open suspend fun getItemByVendorId(vendorId: Int):List<Item>{
        val dao: ItemDAO = GroceerDatabase.getInstance(this).itemDAO
        val respository = ItemRepository(dao)
        var item:List<Item> = respository.getItemWithVendorId(vendorId)
        return item
    }

}