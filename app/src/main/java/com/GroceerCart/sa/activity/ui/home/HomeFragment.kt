package com.GroceerCart.sa.activity.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerBarcodeScannerActivity
import com.GroceerCart.sa.activity.GroceerHomeActivity
import com.GroceerCart.sa.activity.HomeSearchActivity
import com.GroceerCart.sa.adapter.*
import com.GroceerCart.sa.api.GroceerApiInstance
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.tax.Tax
import com.GroceerCart.sa.db.tax.TaxDAO
import com.GroceerCart.sa.db.tax.TaxDatabase
import com.GroceerCart.sa.db.tax.TaxRepository
import com.GroceerCart.sa.service.GroceerApiService
import com.GroceerCart.sa.service.homeservice.*
import com.GroceerCart.sa.ui.AutoScrollViewPager
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class HomeFragment : Fragment() {
    private val LOG_TAG = "Barcode Scanner API"
    private val PHOTO_REQUEST = 10
    private var scanResults: TextView? = null
    private var decode: TextView? = null
    private var detector: BarcodeDetector? = null
    private var imageUri: Uri? = null
    private val REQUEST_WRITE_PERMISSION = 20
    private val SAVED_INSTANCE_URI = "uri"
    private val SAVED_INSTANCE_RESULT = "result"
    private var currImagePath: String? = null
    internal var imageFile: File? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var edtHomeSearch:EditText
    private lateinit var vendorId:String
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var groceerApiService: GroceerApiService
    private lateinit var  bannerList :List<Table>
    private lateinit var  freshList :List<Table2>
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var viewPager:AutoScrollViewPager
    private lateinit var tabs:TabLayout
    private lateinit var homeScrollView:NestedScrollView
    private lateinit var activity : GroceerHomeActivity
    private lateinit var productScanner:ImageView

    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var daily_adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var bank_offers_adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var productAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var topBrandAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    var priceSymmbol:String = ""
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        Log.e("Progress Bar " , "onCreateView" )
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        activity  = context as GroceerHomeActivity
        sharedPreferences = activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        progressBar = root.findViewById(R.id.homeProgressBar)
        homeScrollView = root.findViewById(R.id.nestedScrollView)
        productScanner = root.findViewById(R.id.imgScanner)
        edtHomeSearch = root.findViewById(R.id.edtHomeSearch)
        homeScrollView.visibility = View.GONE
        if (!progressBar.isVisible){
            Log.e("Progress Bar " , "onCreateView - Visible" )
            progressBar.visibility = View.VISIBLE
        }
        viewPager = root.findViewById<AutoScrollViewPager>(R.id.top_pager_slider)
        tabs = root.findViewById<TabLayout>(R.id.tabs)


        groceerApiService = GroceerApiInstance.getRetrofitInstance(activity.vendor_code).create(GroceerApiService::class.java)
        layoutManager = LinearLayoutManager(getActivity())


        productScanner.setOnClickListener {
            startActivity(Intent(activity, GroceerBarcodeScannerActivity::class.java))


            //intiLocationPermission()
            //ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
        }

        edtHomeSearch.setOnClickListener {
            startActivity(Intent(activity, HomeSearchActivity::class.java))
        }

        val recyclerView = root.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager

        val textBankOffers = root.findViewById<TextView>(R.id.txt_offers)
        val textBank = getColoredSpanned("BANK", "#000000")
        val textOffer = getColoredSpanned("OFFERS", "#0f818a")
        textBankOffers.text = Html.fromHtml(textBank + " " + textOffer)

        val textTrendingProducts = root.findViewById<TextView>(R.id.txt_trending)
        val textTrending = getColoredSpanned("TRENDING", "#000000")
        val textProduct = getColoredSpanned("PRODUCTS", "#0f818a")
        textTrendingProducts.text = Html.fromHtml(textTrending + " " + textProduct)

        val textTopBrands = root.findViewById<TextView>(R.id.txt_topBrand)
        val textTop = getColoredSpanned("TOP", "#000000")
        val textBrand = getColoredSpanned("BRANDS", "#0f818a")
        textTopBrands.text = Html.fromHtml(textTop + " " + textBrand)

        val offerRecyclerView = root.findViewById<RecyclerView>(R.id.recycler__offers_view)
        offerRecyclerView.setHasFixedSize(true)
        var horizontalOfferLayout = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        offerRecyclerView.layoutManager = horizontalOfferLayout

        val dailyrecyclerView = root.findViewById<RecyclerView>(R.id.recycler__horizontal_view)
        dailyrecyclerView.setHasFixedSize(true)
        var horizontalLayout = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        dailyrecyclerView.layoutManager = horizontalLayout

        val productrecyclerView = root.findViewById<RecyclerView>(R.id.recycler__trending_view)
        productrecyclerView.setHasFixedSize(true)
        var horizontalProductLayout = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        productrecyclerView.layoutManager = horizontalProductLayout

        val topBrandrecyclerView = root.findViewById<RecyclerView>(R.id.recycler__top_brand)
        topBrandrecyclerView.setHasFixedSize(true)
        var horizontalTopBrandLayout = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        topBrandrecyclerView.layoutManager = horizontalTopBrandLayout

        return root
    }

    private fun getColoredSpanned(text: String, color: String): String? {
        return "<font color=$color>$text</font>"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var activity : GroceerHomeActivity = context as GroceerHomeActivity
        if (activity.getConnectionStatus()){
            getHomePageDetail(activity.vendor_id,activity.vendor_code,activity.vendor_branchId,activity.vendor_locationId)
        }else{
            Toast.makeText(getActivity(),"Network Connection Error! , Please check your Internet Connection and Try Again",
                Toast.LENGTH_LONG).show()
        }


    }

    private fun imageSliderImplementation(sliderList: List<Table1>) {
        val adapter = activity?.let { HomeBannerAdapter(it,sliderList) }
        viewPager.adapter = adapter
      //  viewPager.currentItem = adapter?.count?.div(2) ?: 0

        tabs.setupWithViewPager(viewPager)
        // start auto scroll
        viewPager.startAutoScroll(10000);
        // set auto scroll time in mili
        viewPager.interval = 10000
        // enable recycling using true
        viewPager.setCycle(true);

    }

    private fun loadFreshItemView(freshList: List<Table2>) {
        adapter = FreshItemAdapter(activity,freshList)
        recycler_view.adapter = adapter
    }

    private fun loadGridItem(){
        val adapter = getActivity()?.let { HomeGridAdapter(it) }

        home_gridView.adapter = adapter
        home_gridView.isExpanded = true
    }

    private fun loadDailyEssentialItemView(dailyList: List<Table7>) {
        daily_adapter = DailyEssentialAdapter(activity,dailyList)
        recycler__horizontal_view.adapter = daily_adapter
    }

    private fun loadBankOffersDetail(offerList: List<Table9>){
        bank_offers_adapter = BankOfferAdapter(this,offerList)
        recycler__offers_view.adapter = bank_offers_adapter
    }

    private fun loadTrendingItemList(productList: List<Table3>) {
        productAdapter = TrendingProductAdapter(activity,productList,priceSymmbol)
        recycler__trending_view.adapter = productAdapter
    }

    private fun loadTopBrandList(topBrandList:List<Table8>){
        topBrandAdapter = TopBrandAdapter(activity,topBrandList)
        recycler__top_brand.adapter = topBrandAdapter
    }
    fun getHomePageDetail(
        vendorId: Int,
        vendorCode: String,
        vendorBranchid: Int,
        vendorLocationid: Int
    ) {
        val rootObject= JSONObject()
        rootObject.put("type",63)
        rootObject.put("userId",0)
        rootObject.put("filterId",vendorId)
        rootObject.put("clientId",vendorCode)
        rootObject.put("condition","")
        rootObject.put("clientSeqno",0)
        rootObject.put("searchName","")
        rootObject.put("locationId",vendorLocationid)
        rootObject.put("branchId",vendorBranchid            )
        Log.e("String",rootObject.toString())

        val responseLiveData : LiveData<Response<HomeApi>> = liveData {

            val response = groceerApiService.getHomeDetail(rootObject.toString())
            emit(response)
        }

        responseLiveData.observe(viewLifecycleOwner, Observer {
            val homeResponse = it?.body()
            Log.e("Responce",homeResponse.toString())

            homeScrollView.visibility = View.VISIBLE
            if (progressBar.isVisible){
                Log.e("Progress Bar " , " - GONE" )
                progressBar.visibility = View.GONE
            }

            if (homeResponse!= null){
                Log.e("Responce",homeResponse.objresult.toString())

                val editor:SharedPreferences.Editor =  sharedPreferences.edit()
                priceSymmbol = homeResponse.objresult.table4.get(0).symbol

                editor.putString("priceSymbol",priceSymmbol)
                editor.putInt("decimal",homeResponse.objresult.table4.get(0).decimalPoints)
                editor.apply()
                editor.commit()

                freshList = homeResponse.objresult.table2
                imageSliderImplementation( homeResponse .objresult.table1)
                loadFreshItemView(homeResponse.objresult.table2)
                loadGridItem()
                loadDailyEssentialItemView(homeResponse .objresult.table7)
                loadBankOffersDetail(homeResponse .objresult.table9)
                loadTrendingItemList(homeResponse.objresult.table3)
                loadTopBrandList(homeResponse.objresult.table8)

                var activity : GroceerHomeActivity = context as GroceerHomeActivity
                activity.prepareMenuData(homeResponse.objresult.table,homeResponse.objresult.table6)

                if(homeResponse.objresult.table10.size > 0){
                    CoroutineScope(Dispatchers.IO).launch {
                        addTaxDetailToDB(homeResponse.objresult.table10)
                    }
                }
            }
        })
    }

    private suspend fun addTaxDetailToDB(table10: List<Table10>) {
        var dao: TaxDAO = GroceerDatabase.getInstance(activity).taxDAO
        var repository = TaxRepository(dao)
        if (repository.getAllTax().size>0){
            repository.deleteAllTax()
        }
        for ( tax in table10){
            repository.insertTax(
                Tax(null,tax.seqno,tax.name,tax.taxtype,tax.taxvalue,tax.groupId
            )
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("onRequestPermission", "" + requestCode)
        when (requestCode) {
            REQUEST_WRITE_PERMISSION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture()
            } else {
                Toast.makeText(activity, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("onActivityResult", "" + requestCode + " " + resultCode)
        if (requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = imageUri
            launchMediaScanIntent(mediaScanIntent)
            try {
                val bitmap = decodeBitmapUri(activity, imageUri)
                if (detector!!.isOperational && bitmap != null) {
                    val frame = Frame.Builder().setBitmap(bitmap).build()
                    val barcodes = detector!!.detect(frame)
                    for (index in 0 until barcodes.size()) {
                        val code = barcodes.valueAt(index)
                        scanResults!!.text = scanResults!!.text.toString() + code.displayValue
                        val type = barcodes.valueAt(index).valueFormat
                        when (type) {
                            Barcode.CONTACT_INFO -> Log.i(LOG_TAG, code.contactInfo.title)
                            Barcode.EMAIL -> Log.i(LOG_TAG, code.email.address)
                            Barcode.ISBN -> Log.i(LOG_TAG, code.rawValue)
                            Barcode.PHONE -> Log.i(LOG_TAG, code.phone.number)
                            Barcode.PRODUCT -> Log.i(LOG_TAG, code.rawValue)
                            Barcode.SMS -> Log.i(LOG_TAG, code.sms.message)
                            Barcode.TEXT -> Log.i(LOG_TAG, code.rawValue)
                            Barcode.URL -> Log.i(LOG_TAG, "url: " + code.url.url)
                            Barcode.WIFI -> Log.i(LOG_TAG, code.wifi.ssid)
                            Barcode.GEO -> Log.i(LOG_TAG, code.geoPoint.lat.toString() + ":" + code.geoPoint.lng)
                            Barcode.CALENDAR_EVENT -> Log.i(LOG_TAG, code.calendarEvent.description)
                            Barcode.DRIVER_LICENSE -> Log.i(LOG_TAG, code.driverLicense.licenseNumber)
                            else -> Log.i(LOG_TAG, code.rawValue)
                        }
                    }
                    if (barcodes.size() == 0) {
                        scanResults!!.text = "Scan Failed "
                    }
                } else {
                    scanResults!!.text = "Could not set up the Barcode detector!"
                }
            } catch (e: Exception) {
                Toast.makeText(activity, "Failed to load Image", Toast.LENGTH_SHORT)
                    .show()
                Log.e(LOG_TAG, e.toString())
            }

        }
    }

    private fun takePicture() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            imageFile = createImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var authorities: String = activity.applicationContext.packageName + ".fileprovider"



        imageUri = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Uri.fromFile(imageFile)
        } else {
            imageFile?.let { FileProvider.getUriForFile(activity, authorities, it) }
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        if (intent.resolveActivity(activity.packageManager) != null) {
            startActivityForResult(intent, PHOTO_REQUEST)
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = File(Environment.getExternalStorageDirectory(), "picture.jpg")
        if (!storageDir.exists()) {
            storageDir.parentFile.mkdirs()
            storageDir.createNewFile()
        }
        currImagePath = storageDir.absolutePath
        return storageDir
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (imageUri != null) {
            outState!!.putString(SAVED_INSTANCE_URI, imageUri!!.toString())
            outState.putString(SAVED_INSTANCE_RESULT, scanResults!!.text.toString())
        }
        super.onSaveInstanceState(outState)
    }

    private fun launchMediaScanIntent(mediaScanIntent: Intent) {

        activity.sendBroadcast(mediaScanIntent)
    }

    @Throws(FileNotFoundException::class)
    private fun decodeBitmapUri(ctx: Context, uri: Uri?): Bitmap? {

        val targetW = 600
        val targetH = 600
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true

        BitmapFactory.decodeStream(uri?.let { ctx.contentResolver.openInputStream(it) }, null, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        return BitmapFactory.decodeStream(uri?.let {
            ctx.contentResolver
                .openInputStream(it)
        }, null, bmOptions)
    }

    private fun intiLocationPermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)

        /* ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)

         if (ContextCompat.checkSelfPermission(
                 activity,
                 Manifest.permission.ACCESS_FINE_LOCATION
             ) != PackageManager.PERMISSION_GRANTED
         ) {
             if (ActivityCompat.shouldShowRequestPermissionRationale(
                     this,
                     Manifest.permission.ACCESS_FINE_LOCATION
                 )
             ) {
                 ActivityCompat.requestPermissions(
                     this,
                     arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                     1
                 )
             } else {
                 ActivityCompat.requestPermissions(
                     this,
                     arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                     1
                 )
             }
         }else{
             startLocationUpdates()
         }*/
    }



}