package com.GroceerCart.sa.db.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id :Int?,
    @ColumnInfo(name = "firstName")
    val firstName : String?,
    @ColumnInfo(name = "lastName")
    val lastName : String?,
    @ColumnInfo(name = "emailAddress")
    val emailAddress:String?,
    @ColumnInfo(name = "contactNumber")
    val contactNumber:String?,
    @ColumnInfo(name = "address1")
    val address1 :String?,
    @ColumnInfo(name = "address2")
    val address2:String?,
    @ColumnInfo(name = "pinCode")
    val pinCode :String?,
    @ColumnInfo(name = "isActive")
    val isActive: Boolean,
    @ColumnInfo(name = "isDeleted")
    val isDeleted:Boolean?,
    @ColumnInfo(name = "createdDate")
    val createdDate: String?,
    @ColumnInfo(name = "fLogin")
    val fLogin: String?,
    @ColumnInfo(name = "gLogin")
    val gLogin:String?,
    @ColumnInfo(name = "externalUserId")
    val externalUserId: String?,
    @ColumnInfo(name = "profilePic")
    val profilePic:String?
) {
}