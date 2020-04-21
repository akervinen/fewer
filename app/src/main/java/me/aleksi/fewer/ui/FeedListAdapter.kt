package me.aleksi.fewer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.models.ExpandableListPosition
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.android.synthetic.main.list_item_feed.view.*
import kotlinx.android.synthetic.main.list_item_group.view.*
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
                setFeed(feed)
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
        holder?.expand()
    }

    fun expandAll() {
        groups.forEachIndexed { index, _ ->
            val groupIdx = expandableList.getFlattenedGroupIndex(
                ExpandableListPosition.obtain(ExpandableListPosition.GROUP, index, 0, 0)
            )
            if (!isGroupExpanded(groupIdx)) {
                onGroupClick(groupIdx)
            }
        }
    }

    class FeedGroupViewHolder(itemView: View) : GroupViewHolder(itemView) {
        private val arrowIcon: ImageView = itemView.feedList_groupIcon
        private val groupTitle: TextView = itemView.feedList_groupTitle

        fun setGroupTitle(group: ExpandableGroup<*>?) {
            groupTitle.text = group?.title ?: "und"
        }

        override fun expand() {
            super.expand()

            val anim = RotateAnimation(180f, 360f, RELATIVE_TO_SELF, .5f, RELATIVE_TO_SELF, .5f)
            anim.duration = 300
            anim.fillAfter = true
            arrowIcon.animation = anim
        }

        override fun collapse() {
            super.collapse()

            val anim = RotateAnimation(360f, 180f, RELATIVE_TO_SELF, .5f, RELATIVE_TO_SELF, .5f)
            anim.duration = 300
            anim.fillAfter = true
            arrowIcon.animation = anim
        }
    }

    class FeedViewHolder(itemView: View) : ChildViewHolder(itemView) {
        private val feedIcon: ImageView = itemView.feedList_feedIcon
        private val feedTitle: TextView = itemView.feedList_feedTitle

        fun setOnClick(listener: View.OnClickListener?) {
            itemView.setOnClickListener(listener)
        }

        fun setFeed(feed: Feed) {
            feedTitle.text = feed.title
            if (feed.favicon != null)
                feedIcon.setImageBitmap(feed.favicon)
        }
    }
}
