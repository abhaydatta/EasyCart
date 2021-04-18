package com.GroceerCart.sa.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.GroceerCart.sa.db.cart.ItemDAO
import com.GroceerCart.sa.db.db.User
import com.GroceerCart.sa.db.db.UserDAO
import com.GroceerCart.sa.db.db.UserDatabase
import com.GroceerCart.sa.db.db.cart.Item
import com.GroceerCart.sa.db.tax.Tax
import com.GroceerCart.sa.db.tax.TaxDAO
import com.GroceerCart.sa.db.taxitem.TaxItem
import com.GroceerCart.sa.db.taxitem.TaxItemDAO
import com.GroceerCart.sa.db.vendor.Vendor
import com.GroceerCart.sa.db.vendor.VendorDAO

@Database(entities = [User::class,Item::class,Tax::class,Vendor::class,TaxItem::class],version = 1)
abstract class GroceerDatabase:RoomDatabase() {
    abstract val userDao : UserDAO
    abstract val itemDAO: ItemDAO
    abstract val vendorDAO:VendorDAO
    abstract val taxDAO:TaxDAO
    abstract val taxItemDAO:TaxItemDAO
    companion object{
        @Volatile
        private var INSTANCE : GroceerDatabase?= null
        fun getInstance(context: Context): GroceerDatabase {
            synchronized(this){
                var instance: GroceerDatabase?=
                    INSTANCE
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GroceerDatabase::class.java,
                        "groceer"
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }
    }
}