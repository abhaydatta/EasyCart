package com.GroceerCart.sa.service

data class OtpDetailTable(
    val otpnotif_createdDate: String,
    val otpnotif_id: Int,
    val otpnotif_isValidate: Boolean,
    val otpnotif_mobileNo: String,
    val otpnotif_otp: String,
    val otpnotif_validateAt: String
)