package me.aleksi.fewer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import me.aleksi.fewer.fever.FeverServerService
import me.aleksi.fewer.fever.PARAM_FEEDLIST
import me.aleksi.fewer.ui.FeedAdapter
import me.aleksi.fewer.ui.FeedGroup
import me.aleksi.fewer.ui.toUiFeedGroup

class MainActivity : AppCompatActivity() {
    private val feedGroups = mutableListOf<FeedGroup>()

    private val feedAdapter = FeedAdapter(feedGroups)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        val recycler = findViewById<RecyclerView>(R.id.drawer_nav)
        recycler.adapter = feedAdapter

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val server = sharedPreferences.getString(getString(R.string.pref_server), "")
        val hash = sharedPreferences.getString(getString(R.string.pref_hash), "")

        FeverServerService.startActionGetFeeds(this, server!!, hash, FeedGroupReceiver(Handler()))
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
        }
        return super.onOptionsItemSelected(item)
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
                    }
                }
                FeverServerService.ERROR -> {
                }
            }
            super.onReceiveResult(resultCode, resultData)
        }
    }
}
