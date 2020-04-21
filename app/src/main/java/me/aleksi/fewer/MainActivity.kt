package me.aleksi.fewer

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.os.ResultReceiver
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import me.aleksi.fewer.fever.*
import me.aleksi.fewer.ui.*
import me.aleksi.fewer.ui.FeedGroup

class MainActivity : AppCompatActivity(), OnItemClickListener {
    private val feedItems = mutableListOf<FeedItem>()
    private val feedItemsFiltered = mutableListOf<FeedItem>()
    private val feedGroups = mutableListOf<FeedGroup>()

    private val feedAdapter = FeedListAdapter(feedGroups)
    private val feedItemAdapter = FeedItemAdapter(feedItems, this)

    private var activeFeed: Feed? = null

    private var maxItemId: Long? = null

    private lateinit var scrollLoader: RecyclerScrollLoader

    private lateinit var feedItemList: RecyclerView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

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
            Log.d("TAG", "Feed clicked: ${it.title}")
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
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        feedAdapter.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        feedAdapter.onRestoreInstanceState(savedInstanceState)
    }

    private fun loadMore() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val server = sharedPreferences.getString(getString(R.string.pref_server), "")
        val hash = sharedPreferences.getString(getString(R.string.pref_hash), "")

        FeverServerService.startActionGetItems(
            this,
            server!!,
            hash,
            maxItemId,
            activeFeed?.id,
            FeedItemReceiver(maxItemId != null, Handler())
        )
    }

    private fun refresh() {
        maxItemId = null
        loadMore()
    }

    private fun refreshFeedList() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val server = sharedPreferences.getString(getString(R.string.pref_server), "")
        val hash = sharedPreferences.getString(getString(R.string.pref_hash), "")

        FeverServerService.startActionGetFeeds(this, server!!, hash, FeedGroupReceiver(Handler()))
    }

    private fun setActiveFeed(feed: Feed?) {
        if (feed != activeFeed) {
            activeFeed = feed

            title = feed?.title ?: "All Feeds"

            refresh()
        }
    }

    fun clickAllFeeds(view: View) {
        setActiveFeed(null)
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    private fun addFeedItems(items: List<FeedItem>) {
        scrollLoader.loading = false
        scrollLoader.moreToLoad = items.isNotEmpty()

        val posStart = feedItems.size

        feedItems.addAll(items)

        if (items.isNotEmpty()) {
            maxItemId = items.last().id
        }
        feedItemAdapter.notifyItemRangeInserted(posStart, items.size)
    }

    private fun setFeedItems(items: List<FeedItem>) {
        feedItems.clear()
        feedItemsFiltered.clear()

        addFeedItems(items)
        feedItemAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }

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

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onItemClicked(item: FeedItem) {
        CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(item.url))
    }

    private inner class FeedItemReceiver(private val addItems: Boolean, handler: Handler) :
        ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            when (resultCode) {
                FeverServerService.SUCCESS -> {
                    resultData?.getParcelable<FeedItemList>(PARAM_ITEMLIST)?.let {
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
