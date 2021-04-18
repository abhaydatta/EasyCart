package com.GroceerCart.sa.service.address

data class Table(
    val customeraddress_address: String,
    val customeraddress_city: String,
    val customeraddress_contactNo: String,
    val customeraddress_countryId: Int,
    val customeraddress_createdDate: String,
    val customeraddress_customerId: Int,
    val customeraddress_emailAddress: String,
    val customeraddress_firstname: String,
    val customeraddress_id: Int,
    val customeraddress_isActive: Boolean,
    val customeraddress_isDefault: Boolean,
    val customeraddress_isDeleted: Boolean,
    val customeraddress_lastname: String,
    val customeraddress_pinCode: String,
    val customeraddress_state: String
)