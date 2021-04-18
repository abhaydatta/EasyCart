package com.GroceerCart.sa.api

class MenuModel(s: String, hasChildren: Boolean, isGroup: Boolean, s1: String) {
    var menuName: String? = s
    var url:String? = s1
    var hasChildren = hasChildren
    var isGroup:Boolean = isGroup


}