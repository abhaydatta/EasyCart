package com.GroceerCart.sa.service.categoryservice

data class Subcategory(
    val categoryId: Int,
    val code: String,
    val imagePath: String,
    val name: String,
    val productCount: Int,
    val seqno: Int
)