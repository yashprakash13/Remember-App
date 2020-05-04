package save.newwords.vocab.remember.core.options.reminders

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * Dialog fragment Class for showing daily reminder btn time picker
 */
class DailyReminderTimePickerFragment(private val timeSet: OnTimeSet) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, false)
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        setTimeUpInUI(hours = p1, minutes = p2)
    }

    /**
     * set the interface method so that time can be received in the OptionsFragment
     */
    private fun setTimeUpInUI(hours: Int, minutes: Int) {
        timeSet.timeIsSet(hours, minutes)
    }

    /**
     * interface for time is set callback
     */
    interface OnTimeSet {
        fun timeIsSet(hours: Int, minutes: Int)
    }

}