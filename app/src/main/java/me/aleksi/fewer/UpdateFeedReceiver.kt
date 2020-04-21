package me.aleksi.fewer

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import me.aleksi.fewer.fever.FeedItem
import me.aleksi.fewer.fever.FeedItemsResponse
import me.aleksi.fewer.fever.FeverServerService
import me.aleksi.fewer.fever.PARAM_ITEMLIST

class UpdateFeedReceiver : BroadcastReceiver() {
    private var context: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context
        Log.d("UpdateFeedReceiver", "onReceive")

        if (context == null) return

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val allowed = prefs.getBoolean(context.getString(R.string.pref_background), false)

        if (!allowed) {
            // Cancel any repeating alarms and exit
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pending = Intent(context, UpdateFeedReceiver::class.java).let {
                PendingIntent.getBroadcast(context, 0, it, 0)
            }
            alarmMgr.cancel(pending)
            return
        }

        val maxId = prefs.getLong(context.getString(R.string.pref_currentMaxId), 0)
        val server = prefs.getString(context.getString(R.string.pref_server), "")!!
        val hash = prefs.getString(context.getString(R.string.pref_hash), "")!!

        FeverServerService.startActionGetItems(
            context,
            server,
            hash,
            null,
            null,
            maxId,
            FeedItemReceiver(Handler())
        )
    }

    private fun receiveItems(items: List<FeedItem>) {
        if (context == null || items.isEmpty()) return

        if (!items.any { it.is_read == 0 }) {
            // All have been read elsewhere already, ignore
            return;
        }

        val newMax = items.maxBy { it.id }
        if (newMax != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit {
                putLong(context!!.getString(R.string.pref_currentMaxId), newMax.id)
                apply()
            }
        }

        Log.d("UpdateFeedReceiver", "Found new feed items: ${items.size}")

        val contentIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notifMgr =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fewer"

        with(NotificationCompat.Builder(context!!, channelId)) {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(context!!.getString(R.string.notif_title))
            setContentText(context!!.getString(R.string.notif_content))
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(contentIntent)
            setAutoCancel(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(
                    channelId,
                    context!!.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).let {
                    notifMgr.createNotificationChannel(it)
                }
                setChannelId(channelId)
            }
            build()
        }.let {
            notifMgr.notify(0, it)
        }
    }

    private inner class FeedItemReceiver(handler: Handler) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            when (resultCode) {
                FeverServerService.SUCCESS -> {
                    resultData?.getParcelable<FeedItemsResponse>(PARAM_ITEMLIST)?.let {
                        receiveItems(it.items)
                    }
                }
                FeverServerService.ERROR -> {
                }
            }
            super.onReceiveResult(resultCode, resultData)
        }
    }
}
