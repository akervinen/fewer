package me.aleksi.fewer

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
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

                return JSONObject(body).getInt("auth") == 1
            }
        } catch (e: IllegalArgumentException) {
            throw FeverApiException("Invalid server URL", e)
        } catch (e: JSONException) {
            throw FeverApiException("Invalid server response", e)
        } catch (e: Exception) {
            throw FeverApiException(e.message ?: e.toString(), e)
        }
    }
}
