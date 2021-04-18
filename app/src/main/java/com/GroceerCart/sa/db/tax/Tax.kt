package com.GroceerCart.sa.db.tax

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tax")
data class Tax(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int?,
    @ColumnInfo(name = "seqno")
    val seqno:Int,
    @ColumnInfo(name = "name")
    val name:String?,
    @ColumnInfo(name = "taxType")
    val taxType:String?,
    @ColumnInfo(name = "taxValue")
    val taxValue:Double,
    @ColumnInfo(name = "groupId")
    val groupId:Int
) {

}