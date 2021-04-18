package com.GroceerCart.sa.service

import com.GroceerCart.sa.service.addAddress.AddAddress
import com.GroceerCart.sa.service.address.UserAddress
import com.GroceerCart.sa.service.categoryservice.Category
import com.GroceerCart.sa.service.country.Country
import com.GroceerCart.sa.service.deliveryslot.DeliverySlot
import com.GroceerCart.sa.service.homeservice.HomeApi
import com.GroceerCart.sa.service.notify.Notify
import com.GroceerCart.sa.service.orderlist.OrderListDetail
import com.GroceerCart.sa.service.placeorder.PlaceOrder
import com.GroceerCart.sa.service.productDetail.ProductDetail
import com.GroceerCart.sa.service.productitem.ProductItem
import com.GroceerCart.sa.service.productlist.Product
import com.GroceerCart.sa.service.user.UserDetail
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


interface GroceerApiService {

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_checkPinCodes")
     fun postRawJSON(@Body body: String): Response<Vendors>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_sendOTP")
    suspend fun postSendOtpDetail(@Body body: String): Response<Otp>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/GetMasters")
    suspend fun getHomeDetail(@Body body: String): Response<HomeApi>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_getCartCategorySubCategories")
    suspend fun getCategoryList(@Body body: String): Response<Category>

    /*@Headers( "Content-Type: application/json")
    @POST("api/Admin/_getDataIntoChunks")
    suspend fun getProductList(@Body body:String):Response<Product>*/

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_getSegregatedCartProductSearchByFilter")
    suspend fun getProductList(@Body body:String):Response<ProductItem>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_getProductDetails")
    suspend fun getProductDetail(@Body body: String):Response<ProductDetail>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_registerCartCustomer")
    suspend fun getReigsterdUserDetail(@Body body: String):Response<UserDetail>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/GetMasters")
    suspend fun getCustomerAddressList(@Body body: String):Response<UserAddress>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_cartcustomeraddresses")
    suspend fun saveCustomerAddress(@Body body: String):Response<AddAddress>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/GetMasters")
    suspend fun getCountryList(@Body body: String):Response<Country>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_saveCartBilling")
    suspend fun placeOrder(@Body body: String):Response<PlaceOrder>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/GetMasters")
    suspend fun getDeliverySlots(@Body body: String):Response<DeliverySlot>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_saveUpdateNotifyMe")
    suspend fun updateNotifyMe(@Body body: String):Response<Notify>

    @Headers( "Content-Type: application/json")
    @POST("api/Admin/_getOrderDetailsByNumberAndType")
    suspend fun getOrderDetailsList(@Body body: String):Response<OrderListDetail>
}