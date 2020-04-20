package me.aleksi.fewer.ui

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import me.aleksi.fewer.fever.Feed

fun me.aleksi.fewer.fever.FeedGroup.toUiFeedGroup(): FeedGroup {
    return FeedGroup(this.title, this.feeds)
}

class FeedGroup(val name: String, feeds: List<Feed>) : ExpandableGroup<Feed>(name, feeds)
