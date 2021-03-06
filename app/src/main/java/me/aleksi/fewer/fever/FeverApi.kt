package me.aleksi.fewer.fever

import android.graphics.BitmapFactory
import android.util.Base64
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.security.MessageDigest

/**
 * Hash username + password for Fever API use.
 */
fun hashUserPassword(username: String, password: String): String {
    val combined = "$username:$password"
    val bytes = MessageDigest.getInstance("MD5").digest(combined.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

/**
 * Fever API error.
 */
class FeverApiException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Exception) : super(message, cause)
}

/**
 * Implementation of [FeedApi] for Fever.
 *
 * [serverPath] should point to `fever.php` or equivalent, [hash] should be Fever-compatible
 * authentication hash ([hashUserPassword]).
 */
class FeverApi(private val serverPath: String, private val hash: String) : FeedApi {
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

    override fun items(feedId: Long?, maxId: Long?, sinceId: Long?): FeedItemsResponse {
        val reqBodyBuilder = FormBody.Builder()
            .add("api_key", hash)

        if (sinceId != null) {
            reqBodyBuilder.add("since_id", sinceId.toString())
        } else {
            reqBodyBuilder.add("max_id", maxId?.toString() ?: "9999999999999999")
        }

        if (feedId != null)
            reqBodyBuilder.add("feed_ids", feedId.toString())

        val reqBody = reqBodyBuilder.build()

        return moshi.adapter(FeedItemsResponse::class.java).fromJson(
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

        val icons = moshi.adapter(FeedFaviconsResponse::class.java).fromJson(
            doCall("$serverPath?api&favicons", reqBody)
        )!!.favicons.associateBy { it.id }

        feeds.forEach { (_, feed) ->
            val data = icons[feed.favicon_id]?.data

            if (data != null) {
                val cleanedBase64 = data.substringAfter(',')
                val decoded = Base64.decode(cleanedBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                feed.favicon = bitmap
            }
        }

        val rels = groups.feeds_groups.associate {
            Pair(it.group_id, it.feed_ids.split(',').map { id -> id.toLong() })
        }

        for (g in groups.groups) {
            if (!rels.containsKey(g.id)) continue

            g.feeds.addAll(rels[g.id]?.mapNotNull {
                feeds[it]
            }!!)
        }

        return groups.groups
    }

    override fun markItemAsRead(id: Long) {
        val reqBody = FormBody.Builder()
            .add("api_key", hash)
            .add("mark", "item")
            .add("as", "read")
            .add("id", id.toString())
            .build()

        doCall("$serverPath?api", reqBody)
    }
}
