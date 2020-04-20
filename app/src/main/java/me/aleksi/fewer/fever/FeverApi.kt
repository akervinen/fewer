package me.aleksi.fewer.fever

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

class FeverApi(private val serverPath: String, private val hash: String) :
    FeedApi {
    private val httpClient: OkHttpClient = OkHttpClient()
    private val moshi: Moshi = Moshi.Builder().build()

    private fun doCall(url: String, formBody: FormBody, needsAuth: Boolean = true): String {
        try {
            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: throw FeverApiException(
                    "Empty server response"
                )

                if (needsAuth && moshi.adapter(FeverAuthResponse::class.java)
                        .fromJson(body)?.auth != 1
                ) {
                    throw FeverApiException("Invalid username or password")
                }

                return body
            }

        } catch (e: IllegalArgumentException) {
            throw FeverApiException(
                "Invalid server URL",
                e
            )
        } catch (e: IOException) {
            throw FeverApiException(
                "Invalid server response",
                e
            )
        } catch (e: JsonDataException) {
            throw FeverApiException(
                "Invalid server response",
                e
            )
        } catch (e: Exception) {
            throw FeverApiException(
                e.message ?: e.toString(), e
            )
        }
    }

    override fun isAuthenticated(): Boolean {
        val reqBody = FormBody.Builder()
            .add("api_key", hash)
            .build()

        return moshi.adapter(FeverAuthResponse::class.java).fromJson(
            doCall("$serverPath?api", reqBody)
        )?.auth == 1
    }

    override fun items(): FeedItemList {
        val reqBody = FormBody.Builder()
            .add("api_key", hash)
            .add("max_id", "9999999999999999")
            .build()

        return moshi.adapter(FeedItemList::class.java).fromJson(
            doCall("$serverPath?api&items", reqBody)
        )!!
    }

    override fun feeds(): List<FeedGroup> {
        val reqBody = FormBody.Builder()
            .add("api_key", hash)
            .build()

        val groups = moshi.adapter(FeedGroupsResponse::class.java).fromJson(
            doCall("$serverPath?api&groups", reqBody)
        )!!

        val feeds = moshi.adapter(FeedsResponse::class.java).fromJson(
            doCall("$serverPath?api&feeds", reqBody)
        )!!.feeds.associateBy { it.id }

        val rels = groups.feeds_groups.associate {
            Pair(it.group_id, it.feed_ids.split(',').map { id -> id.toLong() })
        }

        for (g in groups.groups) {
            g.feeds.addAll(rels[g.id]?.mapNotNull { it: Long ->
                feeds[it]
            }!!)
        }

        return groups.groups
    }
}
