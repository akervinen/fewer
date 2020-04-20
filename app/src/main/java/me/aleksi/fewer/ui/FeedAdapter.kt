package me.aleksi.fewer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import me.aleksi.fewer.R
import me.aleksi.fewer.fever.Feed

class FeedAdapter(groups: List<FeedGroup>) :
    ExpandableRecyclerViewAdapter<FeedAdapter.FeedGroupViewHolder, FeedAdapter.FeedViewHolder>(
        groups
    ) {

    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): FeedGroupViewHolder {
        val view = LayoutInflater.from(parent!!.context)
            .inflate(R.layout.list_item_group, parent, false)
        return FeedGroupViewHolder(view)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent!!.context)
            .inflate(R.layout.list_item_feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindChildViewHolder(
        holder: FeedViewHolder?,
        flatPosition: Int,
        group: ExpandableGroup<*>?,
        childIndex: Int
    ) {
        holder?.setFeedTitle(group?.items?.get(childIndex) as Feed)
    }

    override fun onBindGroupViewHolder(
        holder: FeedGroupViewHolder?,
        flatPosition: Int,
        group: ExpandableGroup<*>?
    ) {
        holder?.setGroupTitle(group)
    }

    class FeedGroupViewHolder(itemView: View) : GroupViewHolder(itemView) {
        private val groupTitle: TextView = itemView.findViewById(R.id.feedList_groupTitle)

        fun setGroupTitle(group: ExpandableGroup<*>?) {
            groupTitle.text = group?.title ?: "und"
        }
    }

    class FeedViewHolder(itemView: View) : ChildViewHolder(itemView) {
        private val feedTitle: TextView = itemView.findViewById(R.id.feedList_feedTitle)

        fun setFeedTitle(feed: Feed) {
            feedTitle.text = feed.title
        }
    }
}
