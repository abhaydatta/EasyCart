package com.GroceerCart.sa.db.vendor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.GroceerCart.sa.db.db.User
import com.GroceerCart.sa.db.db.UserDAO
import com.GroceerCart.sa.db.db.UserDatabase

@Database(entities = [Vendor::class],version = 1)
abstract class VendorDatabase:RoomDatabase() {
    abstract val vendorDAO : VendorDAO

    companion object{
        @Volatile
        private var INSTANCE : VendorDatabase?= null
        fun getInstance(context: Context): VendorDatabase {
            synchronized(this){
                var instance: VendorDatabase?=
                    INSTANCE
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        VendorDatabase::class.java,
                        "vendor"
                    ).build()
                }
                return instance
            }
        }
    }
}