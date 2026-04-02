package com.ipack.material

import android.R.attr.action
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.IntentCompat
import com.ipack.material.IpackContent.getIcons
import com.ipack.material.IpackKeys.Actions


class IpackReceiver : BroadcastReceiver() {

    private val tag = IpackReceiver::class.simpleName

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(tag, "onReceive: ${intent?.action}")
        when (intent?.action) {
            Actions.NOTIFY -> handleNotify(intent)
            Actions.NOTIFY_CANCEL -> handleNotifyCancel(intent)
            Actions.QUERY_PACKS -> handleQueryPacks(context)
            Actions.QUERY_ICONS -> handleQueryIcons()
            else -> Log.w(tag, "Unknown action: $action")
        }
    }

    private fun handleNotify(intent: Intent) {
        val id = intent.getIntExtra(IpackKeys.Extras.NOTIFICATION_ID, -1)
        val title = intent.getStringExtra(IpackKeys.Extras.NOTIFICATION_TITLE)
        val text = intent.getStringExtra(IpackKeys.Extras.NOTIFICATION_TEXT)
        val notification = IntentCompat.getParcelableExtra(
            intent,
            IpackKeys.Extras.NOTIFICATION,
            Notification::class.java
        )
        val pendingIntent = IntentCompat.getParcelableExtra(
            intent,
            IpackKeys.Extras.NOTIFICATION_PI,
            PendingIntent::class.java
        )

        Log.i(tag, "handleNotify: $id, $title, $text, $notification, $pendingIntent")
        when {
            notification == null -> Log.e(tag, "Notify failed: null notification")
            id == -1 -> Log.e(tag, "Notify failed: no ID specified")
            pendingIntent == null -> Log.e(tag, "Notify failed: no content intent specified")
            title.isNullOrEmpty() -> Log.e(tag, "Notify failed: no title specified")
            else -> {
                Log.e(tag, "${Actions.NOTIFY} not implemented")
            }
        }
    }

    private fun handleNotifyCancel(intent: Intent) {
        val id = intent.getIntExtra(IpackKeys.Extras.NOTIFICATION_ID, -1)
        Log.e(tag, "${Actions.NOTIFY_CANCEL} id: $id not implemented")
    }

    private fun handleQueryPacks(context: Context) {
        val infoBundle = Bundle().apply {
            putString(IpackKeys.Extras.LABEL, IpackContent.LABEL)
            putBoolean(IpackKeys.Extras.ALL_SAME_SIZE, IpackContent.ALL_SAME_SIZE)
            putString(IpackKeys.Extras.ATTRIBUTION, IpackContent.ATTRIBUTION)
        }
        getResultExtras(true).putBundle(context.packageName, infoBundle)
    }

    private fun handleQueryIcons() {
        val bundle = getResultExtras(true)
        getIcons().forEach { icon ->
            bundle.putInt(icon.name, icon.id)
        }
    }
}
