package com.GroceerCart.sa.db.tax

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.GroceerCart.sa.db.cart.ItemDAO
import com.GroceerCart.sa.db.cart.ItemDatabase
import com.GroceerCart.sa.db.db.cart.Item

@Database(entities = [Tax::class],version = 1)
abstract class TaxDatabase:RoomDatabase() {
    abstract val taxDAO: TaxDAO
    companion object{
        @Volatile
        private var INSTANCE : TaxDatabase?= null
        fun getInstance(context: Context): TaxDatabase {
            synchronized(this){
                var instance: TaxDatabase?=
                    INSTANCE
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TaxDatabase::class.java,
                        "user"
                    ).build()
                }
                return instance
            }
        }
    }
}