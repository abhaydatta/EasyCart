package com.GroceerCart.sa.db.db.cart

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.temporal.TemporalAmount

@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id :Int?,
    @ColumnInfo(name = "productId")
    val productId: Int,
    @ColumnInfo(name = "productsParentId")
    val productsParentId: Int,
    @ColumnInfo(name = "categoryId")
    val categoryId:Int,
    @ColumnInfo(name = "subCategoryId")
    val subCategoryId:Int,
    @ColumnInfo(name = "vendorId")
    val vendorId:Int,
    @ColumnInfo(name = "reasonId")
    val reasonId: Int,
    @ColumnInfo(name = "productName")
    val productName:String?,
    @ColumnInfo(name = "categoryName")
    val categoryName:String?,
    @ColumnInfo(name = "subCategoryName")
    val subCategoryName:String?,
    @ColumnInfo(name = "remark")
    val remark:String?,
    @ColumnInfo(name = "imagePath")
    val imagePath:String?,
    @ColumnInfo(name = "cartStockInHand")
    val cartStockInHand : Int,
    @ColumnInfo(name = "rating")
    val rating: Int,
    @ColumnInfo(name = "offerPrice")
    val offerPrice :Double,
    @ColumnInfo(name = "sellingPrice")
    val sellingPrice :Double,
    @ColumnInfo(name = "PurchasePrice")
    val PurchasePrice: Double,
    @ColumnInfo(name = "cartItem")
    val cartItem:Int,
    @ColumnInfo(name = "cartPrice")
    val cartPrice:Double,
    @ColumnInfo(name = "taxAmount")
    val taxAmount:Double,
    @ColumnInfo(name = "discount")
    val discount:Double
) {
}