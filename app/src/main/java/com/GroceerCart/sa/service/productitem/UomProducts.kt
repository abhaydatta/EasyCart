package com.GroceerCart.sa.service.productitem

data class UomProducts(
    val productId: Int,
    val name: String,
    val product_id: Int,
    val products_code: String,
    val products_description: String,
    val products_parentId: Int,
    val clientId: Int,
    val products_categoryId: Int,
    val products_subcategoryId: Int,
    val products_purchasePrice: Int,
    val products_isMatrix: Boolean,
    val products_uomId:Int,
    val products_maxStockLevel: Int,
    val products_minStockLevel: Int,
    val products_salesmenCommission:Int,
    val products_barCode:Int,
    val products_quantity:Int,
    val products_productInfo:String,
    val imagePath:String,
    val rating: Int,
    val isSpicy: Boolean,
    val isVeg: Boolean,
    val sellingPrice: Double,
    val offerPrice: Double,
    val comboName: String,
    val uomProducts: List<Any>,
    val uomProductsCount: Int,
    val cartStockInHand :Int
    )

