package me.aleksi.fewer.fever

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * Icon for a feed.
 *
 * @param[id] favicon id
 * @param[data] favicon binary data in base64 encoded string.
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FeedFavicon(
    val id: Long,
    val data: String
) : Parcelable

/**
 * List of favicons as returned by Fever API.
 *
 * @param[favicons] list of favicons
 */
@JsonClass(generateAdapter = true)
data class FeedFaviconsResponse(
    val favicons: List<FeedFavicon>
)
