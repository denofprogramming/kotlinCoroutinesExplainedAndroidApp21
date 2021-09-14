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

                //TODO - 6 When _job is cancelled, we need to cancel the Call. Hint: use "invokeOnCancellation".


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
                                        //TODO - 1 We have a Resource with a bitmap... lets resume with it

                                    } ?: //TODO - 2 Using the Elvis operator, lets resume with a KotlinNullPointerException with meaningful text.

                                } else {
                                    //TODO - 3 An Unsuccessful response, lets resume with an HttpException
                                }
                            }
                        } catch (t: Throwable) {
                            //TODO - 4 Lets resume with a Throwable caught by the try/catch
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        //TODO - 5 The network Call failed, so lets resume with the Throwable
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