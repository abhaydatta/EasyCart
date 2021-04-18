package com.GroceerCart.sa.activity.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerAddressListActivity
import com.GroceerCart.sa.activity.GroceerOrderDetailListActivity
import com.GroceerCart.sa.activity.GroceerPinActivity
import com.GroceerCart.sa.adapter.AccountAdapter
import com.GroceerCart.sa.db.GroceerDatabase
import com.GroceerCart.sa.db.db.User
import com.GroceerCart.sa.db.db.UserDAO
import com.GroceerCart.sa.db.db.UserDatabase
import com.GroceerCart.sa.db.db.UserRepository
import com.GroceerCart.sa.ui.CircularImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AccountFragment : Fragment() , AccountAdapter.onItemClickListener{

    private lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private val sharedPrefFile = "groceerpreference"
    private lateinit var  sharedPreferences: SharedPreferences
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var recyclerView:RecyclerView
    private lateinit var userPic: String
    private lateinit var userName:TextView
    private lateinit var userEmail:TextView
    private  var userId:Int? = 0
    private lateinit var userProfile :CircularImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var root = inflater.inflate(R.layout.fragment_account, container, false )

        layoutManager = LinearLayoutManager(activity)

        recyclerView = root.findViewById(R.id.recycler_view_account)
        recyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager

        userName = root.findViewById<TextView>(R.id.tvUserName)
        userEmail = root.findViewById<TextView>(R.id.tvUserEmail)
        userProfile = root.findViewById<CircularImageView>(R.id.imgUserProfile)


        CoroutineScope(Dispatchers.IO).launch {
             getAllUser()
        }

        sharedPreferences = activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!

        //var loginMode = sharedPreferences.getString("login_mode","")

        /*if (loginMode == "google"){
            userName.text = resources.getString(R.string.text_hello) + " " + sharedPreferences.getString("login_google_username","")
            userPic = sharedPreferences.getString("login_google_userPic","").toString()
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            Glide.with(requireActivity()).load( Uri.parse(userPic) )
                .apply(requestOptions).into(userProfile)
        }else if (loginMode == "facebook"){
            userName.text = resources.getString(R.string.text_hello) + " " + sharedPreferences.getString("login_fb_username","")
            userPic = sharedPreferences.getString("login_fb_userPic","").toString()
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            Glide.with(requireActivity()).load( Uri.parse(userPic) )
                .apply(requestOptions).into(userProfile)
        }*/

        loadAdapter()
        return root
    }

    private suspend fun getAllUser() {
        val dao: UserDAO = GroceerDatabase.getInstance(requireActivity().applicationContext).userDao
        val respository = UserRepository(dao)
        var user:List<User> = respository.getAllUsers()
        withContext(Dispatchers.Main){
            userName.text =  user.get(0).firstName + " " + user.get(0).lastName
            userEmail.text = user.get(0).emailAddress
            userPic = user.get(0).profilePic.toString()
            userId = user.get(0).id
            if (userPic != null || userPic != "null" ) {
                val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                Glide.with(requireActivity()).load(Uri.parse(userPic))
                    .apply(requestOptions).into(userProfile)
            }
        }
    }

    private fun loadAdapter() {
        var accountItems: Array<String> =
            this.resources?.getStringArray(R.array.account_item)
        adapter = AccountAdapter(activity,accountItems,this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(position: Int) {
        when(position){
            0 -> startActivity(Intent(activity,GroceerOrderDetailListActivity::class.java).putExtra("customerId",userId))
            1 -> startActivity(Intent(activity,GroceerAddressListActivity::class.java).putExtra("customerId",userId))
            4 -> openLanguageDialog()
            5 -> startActivity(Intent(activity,GroceerPinActivity::class.java))
        }
    }

    private fun openLanguageDialog() {
        val builderSingle: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builderSingle.setTitle("Select Language :-")

        val arrayAdapter: ArrayAdapter<String?> = object :
            ArrayAdapter<String?>(activity?.applicationContext!!, android.R.layout.simple_list_item_1) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                val text1 =
                    view.findViewById<View>(android.R.id.text1) as TextView
                text1.setTextColor(Color.BLACK)
                return view
            }
        }
        arrayAdapter?.add("English")
        arrayAdapter?.add("Arabic")


        builderSingle.setNegativeButton("cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

        builderSingle.setAdapter(arrayAdapter,
            DialogInterface.OnClickListener { dialog, which ->
                val strName = arrayAdapter?.getItem(which)
            })
        builderSingle.show()
    }
}