package me.aleksi.fewer.ui

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import me.aleksi.fewer.R
import me.aleksi.fewer.fever.FeedItem
import me.aleksi.fewer.fever.FeedItemList
import me.aleksi.fewer.fever.FeverServerService
import me.aleksi.fewer.fever.PARAM_ITEMLIST

class FeedFragment : Fragment(), OnItemClickListener {
    private val feedItems = mutableListOf<FeedItem>()

    private lateinit var root: View
    private lateinit var adapter: FeedItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_all_feeds, container, false)

        val list = root as RecyclerView
        adapter = FeedItemAdapter(feedItems, this)
        list.adapter = adapter

        refresh()

        return root
    }

    fun refresh() {
        val context = context ?: return

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val server = sharedPreferences.getString(getString(R.string.pref_server), "")
        val hash = sharedPreferences.getString(getString(R.string.pref_hash), "")

        val receiver = ItemReceiver(Handler())
        FeverServerService.startActionGetItems(context, server!!, hash, receiver)
    }

    private inner class ItemReceiver(handler: Handler) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            when (resultCode) {
                FeverServerService.SUCCESS -> {
                    resultData?.getParcelable<FeedItemList>(PARAM_ITEMLIST)?.let {
                        val adapter =
                            (root as RecyclerView).adapter as FeedItemAdapter
                        feedItems.clear()
                        feedItems.addAll(it.items)
                        adapter.notifyDataSetChanged()
                    }
                }
                FeverServerService.ERROR -> {
                }
            }
            super.onReceiveResult(resultCode, resultData)
        }
    }

    override fun onItemClicked(item: FeedItem) {
        context?.let { CustomTabsIntent.Builder().build().launchUrl(it, Uri.parse(item.url)) }
    }
}
