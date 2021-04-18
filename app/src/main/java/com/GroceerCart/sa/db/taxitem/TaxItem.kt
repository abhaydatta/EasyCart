package com.GroceerCart.sa.db.taxitem

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taxitem")
data class TaxItem (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int?,
    @ColumnInfo(name = "productId")
    val productId: Int?,
    @ColumnInfo(name = "vendorId")
    val vendorId:Int,
    @ColumnInfo(name = "seqno")
    val seqno:Int,
    @ColumnInfo(name = "name")
    val name:String?,
    @ColumnInfo(name = "taxType")
    val taxType:String?,
    @ColumnInfo(name = "taxValue")
    val taxValue:Double,
    @ColumnInfo(name = "taxPrice")
    val taxPrice:Double,
    @ColumnInfo(name = "groupId")
    val groupId:Int
){
}