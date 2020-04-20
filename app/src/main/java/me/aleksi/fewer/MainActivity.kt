package me.aleksi.fewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import me.aleksi.fewer.fever.*
import me.aleksi.fewer.ui.*
import me.aleksi.fewer.ui.FeedGroup

class MainActivity : AppCompatActivity(), OnItemClickListener {
    private val feedItems = mutableListOf<FeedItem>()
    private val feedItemsFiltered = mutableListOf<FeedItem>()
    private val feedGroups = mutableListOf<FeedGroup>()

    private val feedAdapter = FeedListAdapter(feedGroups)
    private val feedItemAdapter = FeedItemAdapter(feedItemsFiltered, this)

    private var activeFeed: Feed? = null

    private lateinit var feedItemList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        feedItemList = findViewById(R.id.feed)
        feedItemList.adapter = feedItemAdapter

        feedAdapter.onFeedClick = {
            Log.d("TAG", "Feed clicked: ${it.title}")
            activeFeed = it
            filterFeedItems(activeFeed)
        }

        val recycler = findViewById<RecyclerView>(R.id.drawer_nav)
        recycler.adapter = feedAdapter

        refreshFeedList()
        refresh()
    }

    private fun refresh() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val server = sharedPreferences.getString(getString(R.string.pref_server), "")
        val hash = sharedPreferences.getString(getString(R.string.pref_hash), "")

        FeverServerService.startActionGetItems(this, server!!, hash, FeedItemReceiver(Handler()))
    }

    private fun refreshFeedList() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val server = sharedPreferences.getString(getString(R.string.pref_server), "")
        val hash = sharedPreferences.getString(getString(R.string.pref_hash), "")

        FeverServerService.startActionGetFeeds(this, server!!, hash, FeedGroupReceiver(Handler()))
    }

    private fun addFeedItems(items: List<FeedItem>) {
        feedItems.clear()
        feedItems.addAll(items)

        filterFeedItems(activeFeed)
    }

    private fun filterFeedItems(feedToShow: Feed?) {
        feedItemsFiltered.clear()
        if (feedToShow != null) {
            feedItemsFiltered.addAll(feedItems.filter { it.feed_id == feedToShow.id })
        } else {
            feedItemsFiltered.addAll(feedItems)
        }
        feedItemAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingsAction -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.refreshAction -> {
                refresh()
                refreshFeedList()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClicked(item: FeedItem) {
        CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(item.url))
    }

    private inner class FeedItemReceiver(handler: Handler) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            when (resultCode) {
                FeverServerService.SUCCESS -> {
                    resultData?.getParcelable<FeedItemList>(PARAM_ITEMLIST)?.let {
                        addFeedItems(it.items)
                    }
                }
                FeverServerService.ERROR -> {
                }
            }
            super.onReceiveResult(resultCode, resultData)
        }
    }

    private inner class FeedGroupReceiver(handler: Handler) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            when (resultCode) {
                FeverServerService.SUCCESS -> {
                    resultData?.getParcelableArrayList<me.aleksi.fewer.fever.FeedGroup>(
                        PARAM_FEEDLIST
                    )?.let {
                        feedGroups.clear()
                        feedGroups.addAll(it.map { group -> group.toUiFeedGroup() })
                        feedAdapter.notifyDataSetChanged()
                        feedAdapter.expandAll()
                    }
                }
                FeverServerService.ERROR -> {
                }
            }
            super.onReceiveResult(resultCode, resultData)
        }
    }
}
