package me.aleksi.fewer.fever

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.widget.Toast
import me.aleksi.fewer.R

private const val ACTION_TEST_SERVER = "me.aleksi.fewer.action.TEST_SERVER"
private const val ACTION_GET_ITEMS = "me.aleksi.fewer.action.GET_ITEMS"
private const val ACTION_GET_FEEDS = "me.aleksi.fewer.action.GET_FEEDS"

private const val EXTRA_SERVER = "me.aleksi.fewer.extra.SERVER"
private const val EXTRA_HASH = "me.aleksi.fewer.extra.HASH"
private const val EXTRA_RECEIVER = "me.aleksi.fewer.extra.RECEIVER"
private const val EXTRA_FEED_ID = "me.aleksi.fewer.extra.FEED_ID"
private const val EXTRA_MAX_ID = "me.aleksi.fewer.extra.MAX_ID"

const val PARAM_ITEMLIST = "me.aleksi.fewer.param.ITEMLIST"
const val PARAM_FEEDLIST = "me.aleksi.fewer.param.FEEDLIST"
const val PARAM_GROUPLIST = "me.aleksi.fewer.param.GROUPLIST"

private const val TAG = "FeverServerService"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
class FeverServerService : IntentService("FeverServerService") {
    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()
        handler = Handler()
    }

    override fun onHandleIntent(intent: Intent?) {
        val server = intent?.getStringExtra(EXTRA_SERVER)
        val hash = intent?.getStringExtra(EXTRA_HASH)
        val receiver = intent?.getParcelableExtra<ResultReceiver>(EXTRA_RECEIVER)

        when (intent?.action) {
            ACTION_TEST_SERVER -> {
                handleActionTestServer(server, hash)
            }
            ACTION_GET_ITEMS -> {
                val maxId = if (intent.hasExtra(EXTRA_MAX_ID)) intent.getLongExtra(
                    EXTRA_MAX_ID,
                    0
                ) else null
                val feedId = if (intent.hasExtra(EXTRA_FEED_ID)) intent.getLongExtra(
                    EXTRA_FEED_ID,
                    0
                ) else null
                handleActionGetItems(server, hash, maxId, feedId, receiver)
            }
            ACTION_GET_FEEDS -> {
                handleActionGetFeeds(server, hash, receiver)
            }
        }
    }

    private fun handleActionTestServer(server: String?, hash: String?) {
        if (server == null) return

        try {
            val client = FeverApi(server, hash.orEmpty())
            val auth = client.isAuthenticated()

            Log.d(TAG, "IsAuthenticated: $auth")
            handler.post {
                Toast.makeText(
                    this,
                    getString(
                        if (auth) {
                            R.string.test_success
                        } else {
                            R.string.test_noauth
                        }
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: FeverApiException) {
            Log.w(TAG, "FeverApiException: $e")
            handler.post {
                Toast.makeText(
                    this,
                    getString(R.string.test_error, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleActionGetItems(
        server: String?,
        hash: String?,
        maxId: Long?,
        feedId: Long?,
        receiver: ResultReceiver?
    ) {
        if (server == null) return

        try {
            val client = FeverApi(server, hash.orEmpty())
            val items = client.items(maxId, feedId)

            val bundle = Bundle()
            bundle.putParcelable(PARAM_ITEMLIST, items)
            receiver?.send(SUCCESS, bundle)
        } catch (e: FeverApiException) {
            Log.w(TAG, "FeverApiException: $e")
            handler.post {
                Toast.makeText(
                    this,
                    getString(R.string.test_error, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleActionGetFeeds(server: String?, hash: String?, receiver: ResultReceiver?) {
        if (server == null) return

        try {
            val client = FeverApi(server, hash.orEmpty())
            val feeds = client.feeds()

            val bundle = Bundle()
            bundle.putParcelableArrayList(PARAM_FEEDLIST, ArrayList(feeds))
            receiver?.send(SUCCESS, bundle)
        } catch (e: FeverApiException) {
            Log.w(TAG, "FeverApiException: $e")
            handler.post {
                Toast.makeText(
                    this,
                    getString(R.string.test_error, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        const val SUCCESS = 1313
        const val ERROR = 4242

        /**
         * Starts this service to perform action TestServer with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun startActionTestServer(context: Context, server: String, hash: String?) {
            val intent = Intent(context, FeverServerService::class.java).apply {
                action = ACTION_TEST_SERVER
                putExtra(EXTRA_SERVER, server)
                putExtra(EXTRA_HASH, hash)
            }
            context.startService(intent)
        }

        /**
         * Starts this service to perform action GetItems with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun startActionGetItems(
            context: Context,
            server: String,
            hash: String?,
            maxId: Long?,
            feedId: Long?,
            receiver: ResultReceiver
        ) {
            val intent = Intent(context, FeverServerService::class.java).apply {
                action = ACTION_GET_ITEMS
                putExtra(EXTRA_SERVER, server)
                putExtra(EXTRA_HASH, hash)
                if (maxId != null)
                    putExtra(EXTRA_MAX_ID, maxId)
                if (feedId != null)
                    putExtra(EXTRA_FEED_ID, feedId)
                putExtra(EXTRA_RECEIVER, receiver)
            }
            context.startService(intent)
        }

        @JvmStatic
        fun startActionGetFeeds(
            context: Context,
            server: String,
            hash: String?,
            receiver: ResultReceiver
        ) {
            val intent = Intent(context, FeverServerService::class.java).apply {
                action = ACTION_GET_FEEDS
                putExtra(EXTRA_SERVER, server)
                putExtra(EXTRA_HASH, hash)
                putExtra(EXTRA_RECEIVER, receiver)
            }
            context.startService(intent)
        }
    }
}
