package com.denofprogramming.mycoroutineapplication.repository.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.denofprogramming.mycoroutineapplication.network.NetworkService
import com.denofprogramming.mycoroutineapplication.network.NetworkServiceApi
import com.denofprogramming.mycoroutineapplication.network.allImages
import com.denofprogramming.mycoroutineapplication.shared.Resource
import com.denofprogramming.mycoroutineapplication.shared.uilt.logMessage
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RetrofitImageRepository(
    private val networkService: NetworkService
) {


    private var _count: Int = -1

    private var _job = Job()


    fun cancel() {
        _job.cancel()
        _job = Job()
    }


    suspend fun fetchImage(imageId: String): Resource<Bitmap> =
        withContext(_job) {
            suspendCancellableCoroutine { continuation ->


                val call: Call<ResponseBody> = networkService.getImage(imageId)

                continuation.invokeOnCancellation {
                    logMessage("Start invokeOnCancellation...")
                    it?.let {
                        logMessage("call.cancel() ${it.javaClass.simpleName} message: ${it.localizedMessage}")
                        call.cancel()
                    }
                }

                call.enqueue(object : Callback<ResponseBody> {

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        try {
                            with(response) {
                                if (isSuccessful) {
                                    body()?.let { body ->
                                        val bitmap = BitmapFactory.decodeStream(body.byteStream())
                                        val resource = Resource.success(bitmap)
                                        continuation.resume(resource)
                                    } ?: continuation.resumeWithException(
                                        KotlinNullPointerException("Oops!! Response body was null")
                                    )
                                } else {
                                    continuation.resumeWithException(HttpException(this))
                                }
                            }
                        } catch (t: Throwable) {
                            continuation.resumeWithException(t)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }
                })
            }
        }


    fun nextImageId(): String {
        _count++
        if (_count > allImages.size - 1) {
            _count = 0
        }
        return allImages[_count]
    }


    companion object {

        fun build(networkService: NetworkService): RetrofitImageRepository {
            return RetrofitImageRepository(networkService)
        }
    }

}