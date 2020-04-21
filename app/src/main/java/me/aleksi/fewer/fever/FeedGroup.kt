package me.aleksi.fewer.fever

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * A group of feeds.
 *
 * Each group has its own id, title and list of feeds in it.
 *
 * @param[id] group's id
 * @param[title] group's title
 *
 * @param[feeds] list of feeds in group (not included in API response)
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FeedGroup(
    val id: Long,
    val title: String,

    @Transient
    val feeds: MutableList<Feed> = mutableListOf()
) : Parcelable

/**
 * List of groups as returned by Fever API.
 *
 * @param[groups] list of [FeedGroup]s
 * @param[feeds_groups] list of [FeedsGroupsRelationships]
 */
@JsonClass(generateAdapter = true)
data class FeedGroupsResponse(
    val groups: List<FeedGroup>,
    val feeds_groups: List<FeedsGroupsRelationships>
)
