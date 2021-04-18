package com.GroceerCart.sa.db.tax

import androidx.room.*
import com.GroceerCart.sa.db.db.cart.Item

@Dao
interface TaxDAO {
    @Insert
    suspend fun insertTax(tax: Tax)

    @Update
    suspend fun updateTax(tax: Tax)

    @Delete
    suspend fun deleteTax(tax: Tax)

    @Query("DELETE FROM tax")
    suspend fun deleteAllTax()

    @Query("SELECT * FROM tax")
    suspend fun getAllTax():List<Tax>

    @Query ("SELECT * FROM tax WHERE groupId =:id")
    suspend fun getTaxWithProductParentId(id:Int):List<Tax>
}