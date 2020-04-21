package me.aleksi.fewer.ui

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_feed_item.view.*
import me.aleksi.fewer.R
import me.aleksi.fewer.fever.FeedItem

/**
 * [RecyclerView.Adapter] that can display a [FeedItem].
 */
class FeedItemAdapter(
    private var values: MutableList<FeedItem>,
    private val clickListener: OnItemClickListener
) : RecyclerView.Adapter<FeedItemAdapter.ViewHolder>() {

    /**
     * Create ViewHolder for an item, using `fragment_feed_item`.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_feed_item, parent, false)
        return ViewHolder(view)
    }

    /**
     * Bind ViewHolder to a [FeedItem].
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.bind(item, clickListener)
    }

    /**
     * Item count.
     */
    override fun getItemCount(): Int = values.size

    /**
     * ViewHolder that sets item title, feed icon and unread status (as bold text).
     */
    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val feedIcon = view.feedIcon
        private val contentView: TextView = view.itemTitle

        /**
         * Bind given [item] to this ViewHolder, with [clickListener] to handle item clicks.
         */
        fun bind(item: FeedItem, clickListener: OnItemClickListener) {
            feedIcon.setImageBitmap(item.feed?.favicon)
            contentView.text = item.title
            if (item.is_read == 0)
                contentView.setTypeface(null, Typeface.BOLD)
            else
                contentView.setTypeface(null, Typeface.NORMAL)

            view.setOnClickListener {
                clickListener.onItemClicked(item)
                if (item.is_read == 1)
                    contentView.setTypeface(null, Typeface.NORMAL)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}

/**
 * Functional interface for handling item clicks.
 */
interface OnItemClickListener {
    /**
     * Called when an item is clicked.
     */
    fun onItemClicked(item: FeedItem)
}
