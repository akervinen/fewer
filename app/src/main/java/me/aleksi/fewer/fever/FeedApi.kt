package me.aleksi.fewer.fever

/**
 * Interface for accessing an RSS server API.
 */
interface FeedApi {
    /**
     * Check if authentication is valid.
     */
    fun isAuthenticated(): Boolean

    /**
     * Get list of items.
     *
     * Optionally items can be filtered with [maxId] to get older items, and [feedId] to get items
     * from a specific feed.
     */
    fun items(maxId: Long?, feedId: Long?): FeedItemsResponse

    /**
     * Get list of feeds, organized in groups.
     */
    fun feeds(): List<FeedGroup>

    /**
     * Mark a [FeedItem] as read.
     */
    fun markItemAsRead(id: Long)
}
