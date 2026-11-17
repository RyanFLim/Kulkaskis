package org.umn.kulkaskis.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import org.umn.kulkaskis.MainActivity
import org.umn.kulkaskis.data.FridgeDatabase

class ExpiryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val ctx = applicationContext

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val isTest = inputData.getBoolean("test", false)
        val items = FridgeDatabase.getInstance(ctx).fridgeDao().observeAll().first()
        val expiring = items.filter { it.isExpiringSoon }
        val expiredCount = items.count { it.daysLeft < 0 }

        val text = when {
            expiring.isEmpty() && expiredCount == 0 -> {
                if (!isTest) return Result.success()
                "No items are expiring soon. Your fridge is in good shape!"
            }
            else -> buildString {
                if (expiring.isNotEmpty()) {
                    append("Expiring soon: ${expiring.joinToString { it.name }}")
                }
                if (expiredCount > 0) {
                    if (isNotEmpty()) append(" · ")
                    append("$expiredCount expired")
                }
            }
        }

        val intent = Intent(ctx, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(ctx, ExpiryScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Check your fridge")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(ctx).notify(1001, notification)
        return Result.success()
    }
}