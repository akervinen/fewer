package me.aleksi.fewer.fever

import android.graphics.Bitmap
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * A single feed.
 *
 * @param[id] feed id
 * @param[favicon_id] feed favicon id
 * @param[title] feed title
 * @param[url] feed's RSS URL
 * @param[site_url] feed's site URL
 * @param[is_spark] unused
 * @param[last_updated_on_time] feed's last updated time as UNIX timestamp
 * @param[favicon] favicon converted to a Bitmap
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class Feed(
    val id: Long,
    val favicon_id: Long,
    val title: String,
    val url: String,
    val site_url: String,
    val is_spark: Int,
    val last_updated_on_time: Long,

    @Transient
    var favicon: Bitmap? = null
) : Parcelable

/**
 * List of feeds as returned by Fever API.
 *
 * @param[feeds] list of [Feed]s
 * @param[feeds_groups] list of [FeedsGroupsRelationships]
 */
@JsonClass(generateAdapter = true)
data class FeedsResponse(
    val feeds: List<Feed>,
    val feeds_groups: List<FeedsGroupsRelationships>
)
