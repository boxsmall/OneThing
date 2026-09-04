package com.boxsmall.onething.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.boxsmall.onething.OneThingApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!supportsAction(intent?.action)) return

        val pendingResult = goAsync()
        val app = context.applicationContext as OneThingApplication
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                app.container.refreshReminder()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        internal fun supportsAction(action: String?): Boolean = action in setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
