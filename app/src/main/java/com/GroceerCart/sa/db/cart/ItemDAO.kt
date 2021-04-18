package com.GroceerCart.sa.db.cart

import androidx.room.*
import com.GroceerCart.sa.db.db.cart.Item

@Dao
interface ItemDAO {
    @Insert
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)


    @Query("DELETE FROM item WHERE productId = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM item")
    suspend fun deleteAllItem()

    @Query("SELECT * FROM item")
    suspend fun getAllItems():List<Item>

    @Query("UPDATE item SET cartItem=:itemQty, cartPrice=:cartItemPrice, taxAmount=:taxPrice WHERE productId = :id")
    suspend fun update(itemQty: Int, cartItemPrice:Double, taxPrice:Double, id: Int)

    @Query ("SELECT * FROM item WHERE productId =:id")
    suspend fun getItemWithId(id:Int):List<Item>

    @Query ("SELECT * FROM item WHERE vendorId =:id")
    suspend fun getItemWithVendorId(id:Int):List<Item>
}