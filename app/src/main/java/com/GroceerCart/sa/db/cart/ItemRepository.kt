package com.GroceerCart.sa.db.cart

import com.GroceerCart.sa.db.db.cart.Item

class ItemRepository(private val dao:ItemDAO) {

    suspend fun getAllItem():List<Item>{
        return dao.getAllItems()
    }

    suspend fun insertItem(item: Item){
        dao.insertItem(item)
    }

    suspend fun updateItem(item: Item){
        dao.updateItem(item)
    }

    suspend fun deleteItem(item: Item){
        dao.deleteItem(item)
    }

    suspend fun deleteById(id: Int){
        dao.deleteById(id)
    }

    suspend  fun deleteAllItem(){
        dao.deleteAllItem()
    }

    suspend fun updateItemWithParameter(
        itemQty: Int,
        cartItemPrice: Double,
        taxPrice:Double,
        productId: Int
    ){
        dao.update(itemQty,cartItemPrice,taxPrice,productId)
    }

    suspend fun getItemWithId(id: Int):List<Item>{
        return dao.getItemWithId(id)
    }

    suspend fun getItemWithVendorId(id: Int):List<Item>{
        return dao.getItemWithVendorId(id)
    }
}