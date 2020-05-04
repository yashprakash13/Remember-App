package save.newwords.vocab.remember.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_options.*

import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.common.*
import save.newwords.vocab.remember.core.options.OptionsViewModel
import save.newwords.vocab.remember.core.options.OptionsViewModelFactory
import save.newwords.vocab.remember.core.options.reminders.DailyReminderTimePickerFragment
import kotlin.math.min

class OptionsFragment : Fragment(), DailyReminderTimePickerFragment.OnTimeSet {

    //shared pref edit
    private lateinit var prefEdits : SharedPreferences
    //shared pref edit def and current val ints
    private  var defValueEdits : Int = 0
    private  var currentPrefValEdits = 0

    //shared pref Meanings
    private lateinit var prefMeanings: SharedPreferences
    //shared pref meaning def and current val ints
    private var defValueMeanings : Int  = 0
    private var currentPrefValMeanings : Int = 0

    //shared pref daily reminder
    private lateinit var prefDailyRem : SharedPreferences
    //shared pref daily reminder boolean
    private var valueDailyRemPref : Boolean = false

    //shared pref daily reminder time
    private lateinit var prefDailyRemTime : SharedPreferences
    //shared pref daily reminder def and current values strings
    private var defValueDailyRemTime : String = ""
    private var currentValueDailyRemTime : String = ""

    //viewmodel instance
    private lateinit var viewModel: OptionsViewModel

    //time set
    private var hoursSet : Int = -1
    private var minutesSet : Int = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init viewmodel
        val factory = OptionsViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory).get(OptionsViewModel::class.java)

        //init all prefs
        initAllSharedPrefs()

        //set current values of settings
        setUpUI()

        //when the show meanings by def switch is clicked
        opt_mean_switch.setOnCheckedChangeListener{ _, isChecked ->
            changeSharedPrefsForShowMeanings(isChecked)
        }
        //when tap to edit settings button is clicked
        opt_edit_btn.setOnClickListener {
            changeTapsToEdit()
        }

        //daily reminder button clicked
        opt_set_daily_rem_btn.setOnClickListener {
            showDailyReminderTimePickerDialog()
        }

        //daily reminder delete button is clicked
        opt_del_reminder_chipbtn.setOnClickListener {
            deleteDailyReminder()
        }
    }

    private fun initAllSharedPrefs() {
        //edit pref
        prefEdits = activity?.getSharedPrefsFor(R.string.tap_to_edit_key)!!
        defValueEdits = resources.getInteger(R.integer.one_tap_to_edit_pref)
        currentPrefValEdits = prefEdits.getInt(getString(R.string.tap_to_edit_key), defValueEdits)

        //meanings pref
        prefMeanings = activity?.getSharedPrefsFor(R.string.show_meanings_by_def_key)!!
        defValueMeanings = resources.getInteger(R.integer.show_mean_by_def_pref)
        currentPrefValMeanings = prefMeanings.getInt(getString(R.string.show_meanings_by_def_key), defValueMeanings)

        //daily reminder pref
        prefDailyRem = activity?.getSharedPrefsFor(R.string.daily_rem_pref_key)!!
        valueDailyRemPref = prefDailyRem.getBoolean(getString(R.string.daily_rem_pref_key),
            resources.getBoolean(R.bool.pref_daily_rem_value))

        //daily reminder time pref
        prefDailyRemTime = activity?.getSharedPrefsFor(R.string.daily_rem_time_val_pref_key)!!
        defValueDailyRemTime = getString(R.string.daily_rem_time_val_pref_def_value)
        currentValueDailyRemTime = prefDailyRemTime.getString(getString(R.string.daily_rem_time_val_pref_key),
            defValueDailyRemTime)!!
    }

    private fun setUpUI() {
        //for meanings setting
        opt_mean_switch.isChecked = currentPrefValMeanings == defValueMeanings

        //for tap to edit setting
        if (currentPrefValEdits == defValueEdits) txt_opt_edit.text = getString(R.string.label_one)
        else txt_opt_edit.text = getString(R.string.label_two)

        //daily reminder option has been set(is true)
        if (valueDailyRemPref){
            //get the time from the daily rem time pref and set it
            val time = prefDailyRemTime.getString(getString(R.string.daily_rem_time_val_pref_key),
                getString(R.string.daily_rem_time_val_pref_def_value))
            updateTimeInUI(time!!)

            //show delete reminder chip button
            opt_del_reminder_chipbtn.show()
        }
    }

    private fun changeSharedPrefsForShowMeanings(isChecked: Boolean) {
        //show meanings on
        if (isChecked){
            activity?.changeSharedPrefTo(prefMeanings, R.string.show_meanings_by_def_key,
                resources.getInteger(R.integer.show_mean_by_def_pref),
                resources.getInteger(R.integer.pref_is_of_int_type))
        }else{//don't show meanings is on
            activity?.changeSharedPrefTo(prefMeanings, R.string.show_meanings_by_def_key,
                resources.getInteger(R.integer.dont_show_mean_def_pref),
                resources.getInteger(R.integer.pref_is_of_int_type))
        }
    }

    private fun changeTapsToEdit() {
        showTapsToEditDialog()
    }

    private fun showTapsToEditDialog() {
        val options = arrayOf(getString(R.string.label_one), getString(R.string.label_two))
        val checkItem = if (currentPrefValEdits == defValueEdits) 0
        else 1

        MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.num_tap))
                    .setPositiveButton(getString(R.string.okay_label)){_, _ ->
                        //nothing to do here
                    }
                    .setSingleChoiceItems(options, checkItem) { _, which ->
                        setEditPref(which)
                    }
                    .show()
    }

    /**
     * to set the shared pref for edit
     */
    private fun setEditPref(selected: Int) {
        if (selected == 0){
            //if one is selected
            txt_opt_edit.text = getString(R.string.label_one)
            if (currentPrefValEdits != resources.getInteger(R.integer.one_tap_to_edit_pref)){
                activity?.changeSharedPrefTo(prefEdits, R.string.tap_to_edit_key,
                    resources.getInteger(R.integer.one_tap_to_edit_pref),
                    resources.getInteger(R.integer.pref_is_of_int_type))
            }
        }else{
            //if two is selected
            txt_opt_edit.text = getString(R.string.label_two)
            if (currentPrefValEdits != resources.getInteger(R.integer.two_tap_edit_pref)){
                activity?.changeSharedPrefTo(prefEdits, R.string.tap_to_edit_key,
                    resources.getInteger(R.integer.two_tap_edit_pref),
                    resources.getInteger(R.integer.pref_is_of_int_type))
            }
        }
    }

    /**
     * to show the time picker dialog
     */
    private fun showDailyReminderTimePickerDialog() {
        DailyReminderTimePickerFragment(
            this
        ).show(parentFragmentManager, "timePicker")
    }

    private fun updateTimeInUI(time: String) {
        opt_set_daily_rem_time_txt.text = time
    }

    /**
     * the interface method where the time set in the picker is received
     * @see getFormattedTimeForUI
     * @param hours from time picker
     * @param minutes from time picker
     */
    override fun timeIsSet(hours: Int, minutes: Int) {
        val time = getFormattedTimeForUI(hours, minutes)
        updateTimeInUI(time)
        updateTimeInPref(time)

        //set the time so it can be used to make the work request later in onPause
        hoursSet = hours
        minutesSet = minutes

        startReminder()
       }

    /**
     * to update the shared pref that stores the time set for the reminder,
     * if no time is set, it stores "Not set"
     * also update the bool value of time pref
     */
    private fun updateTimeInPref(time: String?) {
        if (time != null){
            activity?.changeSharedPrefTo(prefDailyRemTime, R.string.daily_rem_time_val_pref_key,
                time, resources.getInteger(R.integer.pref_is_of_string_type))

            activity?.changeSharedPrefTo(prefDailyRem, R.string.daily_rem_pref_key,
                true, resources.getInteger(R.integer.pref_is_of_bool_type))
        }else{
            activity?.changeSharedPrefTo(prefDailyRemTime, R.string.daily_rem_time_val_pref_key,
                null, resources.getInteger(R.integer.pref_is_of_string_type))

            activity?.changeSharedPrefTo(prefDailyRem, R.string.daily_rem_pref_key,
                false, resources.getInteger(R.integer.pref_is_of_bool_type))
        }
    }

    /**
     * when the delete reminder chip button is clicked, delete the reminder
     */
    private fun deleteDailyReminder() {
        //hide the delete button
        opt_del_reminder_chipbtn.dontShow()
        //change to "Not set"
        updateTimeInUI(getString(R.string.daily_rem_time_val_pref_def_value))
        //make time and bool shared prefs null and false resp.
        updateTimeInPref(null)
        //remove the reminder
        cancelReminder()
    }


    private fun startReminder() {
        Log.e("started: ", "req")
        viewModel.makeDailyReminderWorkRequest(hoursSet, minutesSet)
    }

    private fun cancelReminder(){
        viewModel.stopWorkRequest()
        Log.e("canceled: ", "req")
    }

}
