package save.newwords.vocab.remember.core.options.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.ui.MainActivity

class DailyReminderNotificationWorker(private val context: Context, private val params: WorkerParameters)
    : CoroutineWorker(context, params){

    override suspend fun doWork(): Result {
        createReminderNotification()
        return Result.success()
    }

    /**
     * to create the reminder notification
     */
    private fun createReminderNotification() {
        //Define the pending intent, that ie, on click, the notification should land the user
        //to the ListFragment
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(context, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification_remember)
            setContentTitle("Daily Reminder")
            setContentText("Maintain your streak! Enter new words you learned today into Remember!")
            setStyle(NotificationCompat.BigTextStyle().bigText("Maintain your streak! Enter new words you learned today into Remember!"))
            setAutoCancel(false)
            setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            setContentIntent(pendingIntent)
        }

        val notificationManager : NotificationManager = context.
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel  = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0, builder.build())

    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "10001"
        const val NOTIFICATION_CHANNEL_NAME = "Daily Reminder Notification"
        const val WORK_NAME = "Notification work"

    }

}