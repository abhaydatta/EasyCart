package com.GroceerCart.sa.service.productDetail

data class Table(
    val branchId: Any,
    val brandName: Any,
    val categoryId: Int,
    val categoryName: String,
    val clientId: Int,
    val code: String,
    val description: String,
    val imagePath: String,
    val isSpicy: Boolean,
    val isVeg: Boolean,
    val name: String,
    val offerPrice: Double,
    val price: Int,
    val productInfo: Any,
    val rating: Int,
    val sellingPrice: Double,
    val seqno: Int,
    val subcategoryId: Int,
    val subcategoryName: String,
    val symbol: String,
    val uomId: Int,
    val uomName: String,
    val uomProductsCount: Int,
    val products_parentId: Int,
    val cartStockInHand: Int
)