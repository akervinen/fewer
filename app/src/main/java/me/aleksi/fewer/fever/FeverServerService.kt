package me.aleksi.fewer.fever

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import me.aleksi.fewer.R

private const val ACTION_TEST_SERVER = "me.aleksi.fewer.action.TEST_SERVER"
private const val ACTION_GET_ITEMS = "me.aleksi.fewer.action.GET_ITEMS"
private const val ACTION_GET_FEEDS = "me.aleksi.fewer.action.GET_FEEDS"
private const val ACTION_READ_ITEM = "me.aleksi.fewer.action.READ_ITEM"

private const val EXTRA_SERVER = "me.aleksi.fewer.extra.SERVER"
private const val EXTRA_HASH = "me.aleksi.fewer.extra.HASH"
private const val EXTRA_RECEIVER = "me.aleksi.fewer.extra.RECEIVER"
private const val EXTRA_FEED_ID = "me.aleksi.fewer.extra.FEED_ID"
private const val EXTRA_MAX_ID = "me.aleksi.fewer.extra.MAX_ID"
private const val EXTRA_SINCE_ID = "me.aleksi.fewer.extra.SINCE_ID"
private const val EXTRA_ITEM_ID = "me.aleksi.fewer.extra.ITEM_ID"

/**
 * Receiver parameter for a list of items, returned by `GetItems`
 */
const val PARAM_ITEMLIST = "me.aleksi.fewer.param.ITEMLIST"

/**
 * Receiver parameter for a list of feeds, returned by `GetFeeds`
 */
const val PARAM_FEEDLIST = "me.aleksi.fewer.param.FEEDLIST"

private const val TAG = "FeverServerService"

/**
 * A service for handling requests to a Fever-compatible server.
 *
 * Includes helper methods for making requests.
 */
class FeverServerService : JobIntentService() {
    private lateinit var handler: Handler

    /**
     * Service created.
     *
     * Creates a handler for posting message toasts.
     */
    override fun onCreate() {
        super.onCreate()
        handler = Handler()
    }

    /**
     * Handle incoming intent.
     */
    override fun onHandleWork(intent: Intent) {
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
                val sinceId = if (intent.hasExtra(EXTRA_SINCE_ID)) intent.getLongExtra(
                    EXTRA_SINCE_ID,
                    0
                ) else null
                val feedId = if (intent.hasExtra(EXTRA_FEED_ID)) intent.getLongExtra(
                    EXTRA_FEED_ID,
                    0
                ) else null
                handleActionGetItems(server, hash, feedId, maxId, sinceId, receiver)
            }
            ACTION_GET_FEEDS -> {
                handleActionGetFeeds(server, hash, receiver)
            }
            ACTION_READ_ITEM -> {
                val itemId = intent.getLongExtra(EXTRA_ITEM_ID, 0)
                handleActionReadItem(server, hash, itemId)
            }
        }
    }

    /**
     * Test server and authentication.
     */
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

    /**
     * Get list of items, optionally filtering by max id and feed.
     */
    private fun handleActionGetItems(
        server: String?,
        hash: String?,
        feedId: Long?,
        maxId: Long?,
        sinceId: Long?,
        receiver: ResultReceiver?
    ) {
        if (server == null) return

        try {
            val client = FeverApi(server, hash.orEmpty())
            val items = client.items(feedId, maxId, sinceId)

            val bundle = Bundle()
            bundle.putParcelable(PARAM_ITEMLIST, items)
            receiver?.send(SUCCESS, bundle)
        } catch (e: FeverApiException) {
            Log.w(TAG, "FeverApiException: $e")
            receiver?.send(ERROR, null)
            handler.post {
                Toast.makeText(
                    this,
                    getString(R.string.test_error, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Get list of feeds.
     */
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
            receiver?.send(ERROR, null)
            handler.post {
                Toast.makeText(
                    this,
                    getString(R.string.test_error, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Mark item as read.
     */
    private fun handleActionReadItem(server: String?, hash: String?, itemId: Long) {
        if (server == null) return

        try {
            val client = FeverApi(server, hash.orEmpty())
            client.markItemAsRead(itemId)
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
        /**
         * Fever request was successful.
         */
        const val SUCCESS = 1313

        /**
         * Fever request failed.
         */
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
            enqueueWork(context, FeverServerService::class.java, 68497, intent)
        }

        /**
         * Starts this service to perform action GetItems with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * [itemsReceiver] receives a [FeedItemsResponse] object as [PARAM_ITEMLIST] if
         * the request was a [SUCCESS].
         *
         * @see IntentService
         */
        @JvmStatic
        fun startActionGetItems(
            context: Context,
            server: String,
            hash: String,
            feedId: Long?,
            maxId: Long?,
            sinceId: Long?,
            itemsReceiver: ResultReceiver
        ) {
            val intent = Intent(context, FeverServerService::class.java).apply {
                action = ACTION_GET_ITEMS
                putExtra(EXTRA_SERVER, server)
                putExtra(EXTRA_HASH, hash)
                if (maxId != null)
                    putExtra(EXTRA_MAX_ID, maxId)
                if (sinceId != null)
                    putExtra(EXTRA_SINCE_ID, sinceId)
                if (feedId != null)
                    putExtra(EXTRA_FEED_ID, feedId)
                putExtra(EXTRA_RECEIVER, itemsReceiver)
            }
            enqueueWork(context, FeverServerService::class.java, 68497, intent)
        }

        /**
         * Starts this service to perform action GetFeeds with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * [feedsReceiver] receives an [ArrayList] of [FeedGroup]s as [PARAM_FEEDLIST] if
         * the request was a [SUCCESS].
         *
         * @see IntentService
         */
        @JvmStatic
        fun startActionGetFeeds(
            context: Context,
            server: String,
            hash: String,
            feedsReceiver: ResultReceiver
        ) {
            val intent = Intent(context, FeverServerService::class.java).apply {
                action = ACTION_GET_FEEDS
                putExtra(EXTRA_SERVER, server)
                putExtra(EXTRA_HASH, hash)
                putExtra(EXTRA_RECEIVER, feedsReceiver)
            }
            enqueueWork(context, FeverServerService::class.java, 68497, intent)
        }

        /**
         * Starts this service to perform action ReadItem with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun startActionReadItem(
            context: Context,
            server: String,
            hash: String?,
            itemId: Long
        ) {
            val intent = Intent(context, FeverServerService::class.java).apply {
                action = ACTION_READ_ITEM
                putExtra(EXTRA_SERVER, server)
                putExtra(EXTRA_HASH, hash)
                putExtra(EXTRA_ITEM_ID, itemId)
            }
            enqueueWork(context, FeverServerService::class.java, 68497, intent)
        }
    }
}
