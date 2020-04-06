package me.aleksi.fewer

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject

class FeverApiException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Exception) : super(message, cause)
}

class FeverApi(private val serverPath: String) : FeedApi {
    private val httpClient: OkHttpClient = OkHttpClient()

    override fun isAuthenticated(): Boolean {
        try {
            val request = Request.Builder().url("$serverPath?api").build()
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
