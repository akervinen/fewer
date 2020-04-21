package me.aleksi.fewer.fever

interface FeedApi {
    fun isAuthenticated(): Boolean

    fun items(maxId: Long?, feedId: Long?): FeedItemList

    fun feeds(): List<FeedGroup>

    fun markItemAsRead(id: Long)
}
