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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_feed_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.bind(item, clickListener)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val feedIcon = view.feedIcon
        private val contentView: TextView = view.itemTitle

        fun bind(item: FeedItem, clickListener: OnItemClickListener) {
            feedIcon.setImageBitmap(item.feed?.favicon)
            contentView.text = item.title
            if (item.is_read == 0)
                contentView.setTypeface(contentView.typeface, Typeface.BOLD)

            view.setOnClickListener {
                clickListener.onItemClicked(item)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}

interface OnItemClickListener {
    fun onItemClicked(item: FeedItem)
}
