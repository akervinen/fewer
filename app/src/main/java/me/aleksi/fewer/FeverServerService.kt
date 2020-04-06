package me.aleksi.fewer

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject

private const val ACTION_TEST_SERVER = "me.aleksi.fewer.action.TEST_SERVER"
private const val EXTRA_SERVER = "me.aleksi.fewer.extra.SERVER"

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
                handleActionTestServer(server)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionTestServer(server: String?) {
        val client = OkHttpClient()
        try {
            val request = Request.Builder().url("$server?api").build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                Log.d(TAG, "Response: $body")
                if (body == null) {
                    handler.post {
                        Toast.makeText(
                            this,
                            getString(R.string.test_error_response),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    JSONObject(body).getInt("api_version")
                    handler.post {
                        Toast.makeText(
                            this,
                            getString(R.string.test_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "IllegalArgumentException: $e")
            handler.post {
                Toast.makeText(
                    this,
                    getString(R.string.test_error_url),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: JSONException) {
            Log.w(TAG, "JSONException: $e")
            handler.post {
                Toast.makeText(
                    this,
                    getString(R.string.test_error_response_long),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception: $e")
            handler.post {
                Toast.makeText(
                    this,
                    getString(R.string.test_error_connection, e.message),
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
        fun startActionTestServer(context: Context, server: String) {
            val intent = Intent(context, FeverServerService::class.java).apply {
                action = ACTION_TEST_SERVER
                putExtra(EXTRA_SERVER, server)
            }
            context.startService(intent)
        }
    }
}
