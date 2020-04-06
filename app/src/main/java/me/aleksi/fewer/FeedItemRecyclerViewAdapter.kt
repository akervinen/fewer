package me.aleksi.fewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_feed_item.view.*
import me.aleksi.fewer.fever.FeedItem

/**
 * [RecyclerView.Adapter] that can display a [FeedItem].
 */
class FeedItemRecyclerViewAdapter(
    private var values: List<FeedItem>
) : RecyclerView.Adapter<FeedItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_feed_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.feed_id.toString()
        holder.contentView.text = item.title
    }

    override fun getItemCount(): Int = values.size

    fun updateData(items: List<FeedItem>) {
        values = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.item_number
        val contentView: TextView = view.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}
