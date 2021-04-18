package com.GroceerCart.sa.db.taxitem

import androidx.room.*
import com.GroceerCart.sa.db.tax.Tax
@Dao
interface TaxItemDAO {
    @Insert
    suspend fun insertTax(tax: TaxItem)

    @Update
    suspend fun updateTax(tax: TaxItem)

    @Query("UPDATE taxitem SET  taxPrice=:price WHERE id = :id")
    suspend fun updateTaxWithId(price:Double,id:Int)

    @Delete
    suspend fun deleteTax(tax: TaxItem)

    @Query("DELETE FROM taxitem")
    suspend fun deleteAllTax()

    @Query("DELETE FROM taxitem WHERE productId = :id")
    suspend fun deleteTaxById(id: Int)

    @Query("SELECT * FROM taxitem")
    suspend fun getAllTaxItem():List<TaxItem>

    @Query("SELECT * FROM taxitem WHERE productId =:id")
    suspend fun getTaxItemWithProductId(id:Int):List<TaxItem>

    @Query("SELECT * FROM taxitem WHERE vendorId =:id")
    suspend fun getTaxItemWithVendorId(id:Int):List<TaxItem>
}