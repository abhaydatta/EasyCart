package com.GroceerCart.sa.service.deliveryslot

data class Table(
    val clientid: Int,
    val deliveryslots_code: String,
    val deliveryslots_cutoffTime: String,
    val deliveryslots_description: String,
    val deliveryslots_fromTime: String,
    val deliveryslots_id: Int,
    val deliveryslots_isActive: Boolean,
    val deliveryslots_isDeleted: Boolean,
    val deliveryslots_name: String,
    val deliveryslots_status: String,
    val deliveryslots_toTime: String
)