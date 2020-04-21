package me.aleksi.fewer.fever

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * Single item in a feed.
 *
 * @param[id] item's id
 * @param[feed_id] id of [Feed] item belongs in
 * @param[title] item's title
 * @param[author] item's author
 * @param[html] item's content as HTML
 * @param[url] item's URL
 * @param[is_saved] whether item is saved: 0 or 1
 * @param[is_read] whether item is read: 0 or 1
 * @param[created_on_time] item's creation time as UNIX timestamp
 *
 * @param[feed] [Feed] the item belongs in (not included in API response)
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FeedItem(
    val id: Long,
    val feed_id: Long,
    val title: String,
    val author: String,
    val html: String,
    val url: String,
    val is_saved: Int,
    var is_read: Int,
    val created_on_time: Long,

    @Transient
    var feed: Feed? = null
) : Parcelable

/**
 * List of items as returned by Fever API.
 *
 * @param[total_items] amount of items in response
 * @param[items] list of [FeedItem]s
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FeedItemsResponse(
    val total_items: Int,
    val items: List<FeedItem>
) : Parcelable
