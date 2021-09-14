package com.denofprogramming.mycoroutineapplication.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL = "https://mars.nasa.gov/system/downloadable_items/"


interface NetworkService {

    @GET("{imageId}")
    fun getImage(@Path(value = "imageId") imageId: String): Call<ResponseBody>

}


/**
 * Use the Retrofit builder to build a retrofit object.
 */
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .build()


/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object NetworkServiceApi {
    val retrofitService: NetworkService by lazy { retrofit.create(NetworkService::class.java) }


}