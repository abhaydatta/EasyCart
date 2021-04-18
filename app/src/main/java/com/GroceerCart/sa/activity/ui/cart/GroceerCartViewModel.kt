package com.GroceerCart.sa.activity.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GroceerCartViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "No items in cart !"
    }
    val text: LiveData<String> = _text
}