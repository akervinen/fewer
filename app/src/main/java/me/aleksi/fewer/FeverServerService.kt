package me.aleksi.fewer

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast

private const val ACTION_TEST_SERVER = "me.aleksi.fewer.action.TEST_SERVER"
private const val EXTRA_SERVER = "me.aleksi.fewer.extra.SERVER"
private const val EXTRA_HASH = "me.aleksi.fewer.extra.HASH"

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
        when (intent?.action) {
            ACTION_TEST_SERVER -> {
                val server = intent.getStringExtra(EXTRA_SERVER)
                val hash = intent.getStringExtra(EXTRA_HASH)
                handleActionTestServer(server, hash)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
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

    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
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
    }
}
