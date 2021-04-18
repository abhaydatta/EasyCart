package com.GroceerCart.sa.db.vendor

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "vendor" , indices = [Index(value = ["seqno"], unique = true)])
data class Vendor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int?,
    @ColumnInfo(name = "seqno")
    val seqno:Int,
    @ColumnInfo(name = "branchId")
    val branchId: Int,
    @ColumnInfo(name = "clientAddress")
    val clientAddress: String,
    @ColumnInfo(name = "clientCode")
    val clientCode: String,
    @ColumnInfo(name = "clientContact")
    val clientContact: String,
    @ColumnInfo(name = "clientEmail")
    val clientEmail: String,
    @ColumnInfo(name = "clientImage")
    val clientImage: String,
    @ColumnInfo(name = "clientName")
    val clientName: String,
    @ColumnInfo(name = "locationId")
    val locationId: Int,
    @ColumnInfo(name = "lastOrderDate")
    val lastOrderDate:String,
    @ColumnInfo(name = "totalOrders")
    val totalOrders:Int,
    @ColumnInfo(name = "ratings")
    val ratings:Int,
    @ColumnInfo(name = "checkQuantity")
    val checkQuantity:Boolean,
    @ColumnInfo(name = "locations_name")
    val locations_name:String,
    @ColumnInfo(name = "branch_name")
    val branch_name:String

){

}