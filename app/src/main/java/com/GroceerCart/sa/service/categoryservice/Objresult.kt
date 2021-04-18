package com.GroceerCart.sa.service.categoryservice

data class Objresult(
    val code: String,
    val imagePath: String,
    val name: String,
    val seqno: Int,
    val subcategories: List<Subcategory>
)