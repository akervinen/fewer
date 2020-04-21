package me.aleksi.fewer.fever

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * Feeds-groups relationships.
 *
 * Contains a comma-separated list of [feed_ids] belonging to given [group_id].
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FeedsGroupsRelationships(
    val group_id: Long,
    val feed_ids: String
) : Parcelable
