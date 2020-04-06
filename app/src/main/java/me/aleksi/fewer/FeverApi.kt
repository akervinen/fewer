package me.aleksi.fewer

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.security.MessageDigest

fun hashUserPassword(username: String, password: String): String {
    val combined = "$username:$password"
    val bytes = MessageDigest.getInstance("MD5").digest(combined.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

class FeverApiException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Exception) : super(message, cause)
}

class FeverApi(private val serverPath: String, private val hash: String) : FeedApi {
    private val httpClient: OkHttpClient = OkHttpClient()
    private val moshi: Moshi = Moshi.Builder().build()

    override fun isAuthenticated(): Boolean {
        try {
            val reqBody = FormBody.Builder()
                .add("api_key", hash)
                .build()

            val request = Request.Builder()
                .url("$serverPath?api")
                .post(reqBody)
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body =
                    response.body?.string() ?: throw FeverApiException("Empty server response")

                return moshi.adapter(FeverAuthResponse::class.java).fromJson(body)?.auth == 1
            }
        } catch (e: IllegalArgumentException) {
            throw FeverApiException("Invalid server URL", e)
        } catch (e: IOException) {
            throw FeverApiException("Invalid server response", e)
        } catch (e: JsonDataException) {
            throw FeverApiException("Invalid server response", e)
        } catch (e: Exception) {
            throw FeverApiException(e.message ?: e.toString(), e)
        }
    }
}
