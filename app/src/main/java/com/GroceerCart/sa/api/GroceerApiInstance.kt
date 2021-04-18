package com.GroceerCart.sa.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class GroceerApiInstance {

    companion object{
        private lateinit var retrofitInstance:Retrofit
        val BASE_URL = "http://staging.thegroceer.com/"
         val BASE_URL1 = "http://staging.thegroceer.com"

        private val interceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
            this.level = HttpLoggingInterceptor.Level.HEADERS
        }


        fun getRetrofitInstance(clientId: String): Retrofit {
            if (retrofitInstance!= null) return retrofitInstance 

            retrofitInstance =  Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpClient.Builder().apply {
                    this.addInterceptor(interceptor)
                    this.addInterceptor(SupportInterceptor(clientId))
                    this.authenticator(SupportInterceptor(clientId))
                    this.connectTimeout(20,TimeUnit.SECONDS)
                    this.writeTimeout(20,TimeUnit.SECONDS)
                    this.readTimeout(20,TimeUnit.SECONDS)
                }.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
            return retrofitInstance
        }
    }
}