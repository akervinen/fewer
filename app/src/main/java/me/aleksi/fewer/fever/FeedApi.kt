package me.aleksi.fewer.fever

interface FeedApi {
    fun isAuthenticated(): Boolean

    fun items(): FeedItemList
}
