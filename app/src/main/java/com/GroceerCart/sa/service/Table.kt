package com.GroceerCart.sa.service

data class Table(
    val branchId: Int,
    val clientAddress: String,
    val clientCode: String,
    val clientContact: String,
    val clientEmail: String,
    val clientImage: String,
    val clientName: String,
    val locationId: Int,
    val seqno: Int,
    val lastOrderDate:String,
    val totalOrders:Int,
    val ratings:Int,
    val checkQuantity:Boolean,
    val locations_name:String,
    val branch_name :String
)