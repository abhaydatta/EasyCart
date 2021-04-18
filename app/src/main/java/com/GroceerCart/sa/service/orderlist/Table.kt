package com.GroceerCart.sa.service.orderlist

data class Table(
    val billingId: Int,
    val cartmasterorders_id: Int,
    val cartmasterorders_masterOrderNo: String,
    val cartmasterorders_orderStatus: Any,
    val cartmasterorders_status: String,
    val cartmasterorders_totalAmount: Double,
    val customerId: Int,
    val deliveryDate: Any,
    val discountAmount: Int,
    val itemCount: Int,
    val netTotal: Double,
    val orderNo: String,
    val orderStatus: String?,
    val roundOff: Int,
    val taxAmount: Double,
    val totalAmount: Double,
    val transactionDate: String,
    val transactionId: Any
)