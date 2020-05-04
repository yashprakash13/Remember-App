package save.newwords.vocab.remember.core.options

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import save.newwords.vocab.remember.core.options.reminders.DailyReminderNotificationWorker
import save.newwords.vocab.remember.core.options.reminders.DailyReminderNotificationWorker.Companion.WORK_NAME
import java.util.*
import java.util.concurrent.TimeUnit

class OptionsViewModel(private val context: Context) : ViewModel() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    //work manager periodic work request instance
    private lateinit var repeatingWorkRequest : PeriodicWorkRequest

    /**
     * to make the work request for daily reminder by calculating the time between the supplied
     * time and the "now" time, and then adding 24 hours to it so as to start the
     * the reminders from the next day, once a day.
     */
    fun makeDailyReminderWorkRequest(hours: Int, minutes: Int){

        val currentTime= System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        val timeReq = calendar.timeInMillis

        val diffInMillis = timeReq - currentTime
        val hourDiff = diffInMillis/(1000*60*60)

        applicationScope.launch {
            startWorkRequest(hourDiff)
        }
    }

    /**
     * to init the work request for daily reminder
     */
    private fun startWorkRequest(hoursDelay : Long) {
        repeatingWorkRequest = PeriodicWorkRequestBuilder<DailyReminderNotificationWorker>(
            1, TimeUnit.DAYS)
            .setInitialDelay(hoursDelay+24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                repeatingWorkRequest
            )
    }

    /**
     * to cancel the work request when the reminder is removed in settings
     */
    fun stopWorkRequest() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }



}
