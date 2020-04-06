package me.aleksi.fewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import me.aleksi.fewer.fever.FeedItem
import me.aleksi.fewer.fever.FeedItemList
import me.aleksi.fewer.fever.FeverServerService
import me.aleksi.fewer.fever.PARAM_ITEMLIST

class MainActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var adapter: FeedItemAdapter

    val feedItems = mutableListOf<FeedItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = findViewById<RecyclerView>(R.id.feedList)
        adapter = FeedItemAdapter(feedItems, this)
        list.adapter = adapter

        refresh()
    }

    private fun refresh() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val server = sharedPreferences.getString(getString(R.string.pref_server), "")
        val hash = sharedPreferences.getString(getString(R.string.pref_hash), "")

        val receiver = ItemReceiver(Handler())
        FeverServerService.startActionGetItems(this, server!!, hash, receiver)
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
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class ItemReceiver(handler: Handler) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            when (resultCode) {
                FeverServerService.SUCCESS -> {
                    val items = resultData?.getParcelable<FeedItemList>(PARAM_ITEMLIST)
                    if (items != null) {
                        val adapter =
                            findViewById<RecyclerView>(R.id.feedList)?.adapter as FeedItemAdapter?
                        feedItems.clear()
                        feedItems.addAll(items.items)
                        adapter?.notifyDataSetChanged()
                    }
                }
                FeverServerService.ERROR -> {
                }
            }
            super.onReceiveResult(resultCode, resultData)
        }
    }

    override fun onItemClicked(item: FeedItem) {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(this, Uri.parse(item.url))
    }
}
