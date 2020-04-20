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

class FeedListAdapter(groups: List<FeedGroup>) :
    ExpandableRecyclerViewAdapter<FeedListAdapter.FeedGroupViewHolder, FeedListAdapter.FeedViewHolder>(
        groups
    ) {

    var onFeedClick: ((feed: Feed) -> Unit)? = null

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
        with(holder!!) {
            (group?.items?.get(childIndex) as Feed).let { feed: Feed ->
                setFeedTitle(feed)
                setOnClick(View.OnClickListener { onFeedClick?.invoke(feed) })
            }
        }
    }

    override fun onBindGroupViewHolder(
        holder: FeedGroupViewHolder?,
        flatPosition: Int,
        group: ExpandableGroup<*>?
    ) {
        holder?.setGroupTitle(group)
    }

    fun expandAll() {
        groups.forEachIndexed { index, _ ->
            if (!isGroupExpanded(index)) {
                toggleGroup(index)
            }
        }
    }

    class FeedGroupViewHolder(itemView: View) : GroupViewHolder(itemView) {
        private val groupTitle: TextView = itemView.findViewById(R.id.feedList_groupTitle)

        fun setGroupTitle(group: ExpandableGroup<*>?) {
            groupTitle.text = group?.title ?: "und"
        }
    }

    class FeedViewHolder(itemView: View) : ChildViewHolder(itemView) {
        private val feedTitle: TextView = itemView.findViewById(R.id.feedList_feedTitle)

        fun setOnClick(listener: View.OnClickListener?) {
            itemView.setOnClickListener(listener)
        }

        fun setFeedTitle(feed: Feed) {
            feedTitle.text = feed.title
        }
    }
}
