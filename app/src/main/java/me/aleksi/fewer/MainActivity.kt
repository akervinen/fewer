package me.aleksi.fewer

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.os.ResultReceiver
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import me.aleksi.fewer.fever.*
import me.aleksi.fewer.ui.FeedItemAdapter
import me.aleksi.fewer.ui.FeedListAdapter
import me.aleksi.fewer.ui.OnItemClickListener
import me.aleksi.fewer.ui.RecyclerScrollLoader

/**
 * App's main activity.
 *
 * Sets up its own ActionBar, so the theme should not contain one.
 */
class MainActivity : AppCompatActivity(), OnItemClickListener {
    private val feedItems = mutableListOf<FeedItem>()
    private val feedItemsFiltered = mutableListOf<FeedItem>()
    private val feedGroups = mutableListOf<ExpandableGroup<Feed>>()

    private val feedAdapter = FeedListAdapter(feedGroups)
    private val feedItemAdapter = FeedItemAdapter(feedItems, this)

    private var activeFeed: Feed? = null

    private var maxItemId: Long? = null

    private lateinit var scrollLoader: RecyclerScrollLoader

    private lateinit var feedItemList: RecyclerView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private val sharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(this)

    /**
     * Get "server" from preferences.
     */
    private val prefServer
        get() = sharedPreferences.getString(getString(R.string.pref_server), "")!!

    /**
     * Get "hash" from preferences.
     */
    private val prefHash
        get() = sharedPreferences.getString(getString(R.string.pref_hash), "")!!

    /**
     * Get "daynight" from preferences.
     */
    private val prefDayNight
        get() = sharedPreferences.getString(getString(R.string.pref_daynight), "")!!

    /**
     * Activity created.
     *
     * Sets up drawer, item lists and does an initial refresh.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        feedItemList = feed
        feedItemList.adapter = feedItemAdapter

        scrollLoader = RecyclerScrollLoader(feedItemList.layoutManager as LinearLayoutManager) {
            loadMore()
        }

        feedItemList.addOnScrollListener(scrollLoader)

        feedAdapter.onFeedClick = {
            setActiveFeed(it)

            drawer_layout.closeDrawer(GravityCompat.START)
        }

        drawer_nav.adapter = feedAdapter

        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            R.string.feedList_open,
            R.string.feedList_close
        )
        drawer_layout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        refreshFeedList()
        setActiveFeed(null)
        refresh()

        setDefaultNightMode(
            when (prefDayNight) {
                "0" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                "1" -> AppCompatDelegate.MODE_NIGHT_NO
                "2" -> MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    /**
     * Called after Activity is created.
     *
     * Syncs ActionBar and Drawer.
     */
    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        actionBarDrawerToggle.syncState()
    }

    /**
     * Called when Activity configuration changes.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    /**
     * Called when Activity's state is saved.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        feedAdapter.onSaveInstanceState(outState)
    }

    /**
     * Called when Activity's state is restored.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        feedAdapter.onRestoreInstanceState(savedInstanceState)
    }

    /**
     * Load more feed items.
     */
    private fun loadMore() {
        FeverServerService.startActionGetItems(
            this,
            prefServer,
            prefHash,
            maxItemId,
            activeFeed?.id,
            FeedItemReceiver(maxItemId != null, Handler())
        )
    }

    /**
     * Reload all items.
     */
    private fun refresh() {
        maxItemId = null
        loadMore()
    }

    /**
     * Refresh list of feeds.
     */
    private fun refreshFeedList() {
        FeverServerService.startActionGetFeeds(
            this,
            prefServer,
            prefHash,
            FeedGroupReceiver(Handler())
        )
    }

    /**
     * Set active feed and refresh if it changed.
     */
    private fun setActiveFeed(feed: Feed?) {
        title = feed?.title ?: "All Feeds"
        if (feed != activeFeed) {
            activeFeed = feed

            refresh()
        }
    }

    /**
     * When "All Feeds" is clicked in feed drawer.
     */
    fun clickAllFeeds(view: View) {
        setActiveFeed(null)
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    /**
     * Get a feed by id.
     */
    private fun getFeedById(id: Long): Feed? {
        feedGroups.forEach {
            it.items.forEach { feed: Feed ->
                if (feed.id == id) return feed
            }
        }
        return null
    }

    /**
     * Add items to feed item list, and update [FeedItemAdapter].
     *
     * Stops [RecyclerScrollLoader] loading and prevents it from loading more if server gave
     * no items.
     */
    private fun addFeedItems(items: List<FeedItem>) {
        scrollLoader.loading = false
        scrollLoader.moreToLoad = items.isNotEmpty()

        val posStart = feedItems.size

        items.forEach { it.feed = getFeedById(it.feed_id) }

        feedItems.addAll(items)

        if (items.isNotEmpty()) {
            maxItemId = items.last().id
        }
        feedItemAdapter.notifyItemRangeInserted(posStart, items.size)
    }

    /**
     * Clear feed items and adds initial items.
     */
    private fun setFeedItems(items: List<FeedItem>) {
        feedItems.clear()
        feedItemsFiltered.clear()
        feedItemAdapter.notifyDataSetChanged()

        addFeedItems(items)
    }

    /**
     * Create options menu.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Called when an options item is selected.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
            R.id.settingsAction -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.refreshAction -> {
                refreshFeedList()
                refresh()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Called when Back is pressed.
     *
     * Closes drawer before closing app.
     */
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Callback for an item being clicked.
     *
     * Opens browser through [CustomTabsIntent].
     */
    override fun onItemClicked(item: FeedItem) {
        FeverServerService.startActionReadItem(this, prefServer, prefHash, item.id)
        item.is_read = 1
        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(resources.getColor(R.color.ic_launcher_background))
        builder.build().launchUrl(this, Uri.parse(item.url))
    }

    private inner class FeedItemReceiver(private val addItems: Boolean, handler: Handler) :
        ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            when (resultCode) {
                FeverServerService.SUCCESS -> {
                    resultData?.getParcelable<FeedItemsResponse>(PARAM_ITEMLIST)?.let {
                        if (addItems)
                            addFeedItems(it.items)
                        else
                            setFeedItems(it.items)
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
                    resultData?.getParcelableArrayList<FeedGroup>(
                        PARAM_FEEDLIST
                    )?.let {
                        feedGroups.clear()
                        feedGroups.addAll(it.map { group ->
                            ExpandableGroup<Feed>(
                                group.title,
                                group.feeds
                            )
                        })
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
