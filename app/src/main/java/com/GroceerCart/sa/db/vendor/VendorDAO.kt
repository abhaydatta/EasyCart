package com.GroceerCart.sa.db.vendor

import androidx.room.*
import com.GroceerCart.sa.db.db.User

@Dao
interface VendorDAO{
    @Insert
    suspend fun insertVendor(vendor: Vendor)

    @Update
    suspend fun updateVendor(vendor: Vendor)

    @Delete
    suspend fun deleteVendor(vendor: Vendor)

    @Query("DELETE FROM vendor")
    suspend fun deleteAll()

    @Query("SELECT * FROM vendor")
    suspend fun getAllVendor():List<Vendor>
}