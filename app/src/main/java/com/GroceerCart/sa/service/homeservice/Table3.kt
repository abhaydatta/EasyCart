package com.GroceerCart.sa.service.homeservice

data class Table3(
    val productId: Int,
    val code: String,
    val name: String,
    val sellingPrice: Double,
    val products_parentId :Int,
    val imagePath:String,
    val rating : String,
    val offerPrice :Double,
    val uomName :String,
    val uomId :Int,
    val isSpicy :Boolean,
    val isVeg: Boolean,
    val uomProductsCount :Int,
    val comboName :String,
    val cartStockInHand: Int
)