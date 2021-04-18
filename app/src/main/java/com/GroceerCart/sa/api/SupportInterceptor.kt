package com.GroceerCart.sa.api

import okhttp3.*
import java.io.IOException

class SupportInterceptor(id: String) : Interceptor, Authenticator {
    var clientId: String = id
    /**
     * Interceptor class for setting of the headers for every request
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request?.newBuilder()
            ?.addHeader("Content-Type", "application/json")
            ?.addHeader("Accept", "application/json")
            ?.build()
        return chain.proceed(request)
    }
    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {
        var requestAvailable: Request? = null
        try {

            requestAvailable = response?.request?.newBuilder()?.
            addHeader("clientid", clientId)?.build()
            return requestAvailable

        }catch (ex:Exception){}
        return requestAvailable
    }

    /**
     * Authenticator for when the authToken need to be refresh and updated
     * everytime we get a 401 error code
     */




}