package save.newwords.vocab.remember.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_edit_word.*
import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.common.*
import save.newwords.vocab.remember.common.showSnackbar
import save.newwords.vocab.remember.core.editword.EditWordViewModel
import save.newwords.vocab.remember.core.editword.EditWordViewModelFactory
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.db.WordDatabase
import save.newwords.vocab.remember.repository.WordRepository
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class EditWordFragment : Fragment(), View.OnTouchListener {

    //viewmodel instance
    private lateinit var viewModel: EditWordViewModel

    //navigation argument
    private val args: EditWordFragmentArgs by navArgs()

    //local storage root
    private lateinit var root: File

    //temp audio file name while recording
    private lateinit var tempAudioFileName: String

    /**
     * for inflating xml view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_word, container, false)
    }


    /**
     * for all other tasks in the fragment
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get word name clicked as nav argument
        val wordNameClicked = args.wordNameClicked

        //init root local storage
        root = File(requireActivity().getExternalFilesDir("/"), AUDIO_PATH)

        //init viewmodel instance
        val factory = EditWordViewModelFactory(WordRepository(WordDatabase.getInstance(requireActivity())))
        viewModel =  ViewModelProvider(this, factory).get(EditWordViewModel::class.java)

        //get the word from db from its name
        viewModel.getWordFromName(wordNameClicked)

        //name through which the audio is saved temporarily in cache
        tempAudioFileName = "${requireActivity().externalCacheDir!!.absolutePath}/audiorecordtemp.3gp"

        /**
         * onTouch listener for audio add button
         * for tap and hold to record functionality
         */
        btn_edit_add_audio.setOnTouchListener(this)

        //set up the UI when the word is fetched from the db
        viewModel.clickedWordMutable.observe(viewLifecycleOwner, Observer {
            if (it != null){
                //meaning word has been fetched from db
                setUpUI(it)
            }
        })

        //if audio is present or not
        viewModel.isAudioPresent.observe(viewLifecycleOwner, Observer {
            if (!it) {
                //if audio is not present or is deleted
                changeAudioButtonAppearances(true)
            }else{
                changeAudioButtonAppearances(false)
            }
        })

        //if cancel button is pressed, navigate to list fragment without any action
        btn_edit_cancel.setOnClickListener{
            navigateToListFragment()
            showCancelledMessage()
        }

        //if audio btn is clicked
        btn_edit_audio.setOnClickListener {
            //if audio path is not null, meaning play audio for the word
            if (viewModel.clickedWordMutable.value!!.audioPath != null){
                viewModel.playAudio(root)
                showAudioPlayingMessage()
            } else if (viewModel.isAudioAvailableInCache.value!!){
                //play audio if audio has just been recorded and is in cache, but originally
                //the word didn't have one
                viewModel.playAudioFromCache()
                showAudioPlayingMessage()
            } else{
                //if audio path is null, meaning new audio must be recorded
                viewModel.initRecordingMembers()
                prepareUIForRecording()
            }
        }

        btn_edit_delete_audio.setOnClickListener {
            viewModel.prepareToDeleteAudioForWord(root)
            showSnackBarForUndoDeletion()
        }

        //if save button is clicked, save the contents of the edited word
        btn_edit_save.setOnClickListener {
            //if name field is not empty
            if (til_edit_word_name.isValid()){
                viewModel.clickedWordMutable.value!!.name = til_edit_word_name.getString()

                //if meaning field is valid
                if (til_edit_word_meaning.isValid()){
                    viewModel.clickedWordMutable.value!!.meaning = til_edit_word_meaning.getString()
                }else{
                    viewModel.clickedWordMutable.value!!.meaning = null
                }
                //check if audio is present in cache, if yes, then save the audio permanently
                if (viewModel.isAudioAvailableInCache.value!!){
                    //change the audio path for the word
                    viewModel.clickedWordMutable.value!!.audioPath = til_edit_word_name.getString() + ".3gp"
                    viewModel.saveAudioToStorage(root, til_edit_word_name.getString())
                }
                //the else condition in case the audio is to be deleted
                // is already checked in the view model
                //through saveAndUpdate() method

                //save edited word into db
                viewModel.saveAndUpdate(wordNameClicked)

                showSavedMessage()
                navigateToListFragment()

            }else{
                //if the name til is blank
                til_edit_word_name.error = getString(R.string.label_error_word_without_name)
            }
        }

        //------------------------------audio recording functionalities-----------------------------

        viewModel.isRecording.observe(viewLifecycleOwner, Observer {
            if (it){
                //when touch and hold is in effect, meaning, audio is being recorded
                changeAddAudioButtonToShowRecording()
            }else{
                changeAddAudioButtonToShowStoppedRecording()
            }
        })

        viewModel.isRecorded.observe(viewLifecycleOwner, Observer {
            if (it){
                //when add audio button has been released, meaning, audio has been recorded
                //set the audio buttons to new states - hide add audio btn and show the tap
                //to listen button with the play icon
                changeAudioButtonAppearances(false)
                //next, hide the add audio button
                btn_edit_add_audio.dontShow()
            }
        })

        //observe if audio recording has exceeded the time limit
        viewModel.isTimeExceeded.observe(viewLifecycleOwner, Observer {
            if (it){
                viewModel.stopRecording()
                //this is extra to show the user that recording cannot exceed 5 seconds
                showTimeExceededMessage()
            }
        })


        //------------------------------------------------------------------------------------------

    }


    private fun showTimeExceededMessage() {
        showSnackbar(getString(R.string.snack_max_audio_length))
    }

    private fun changeAddAudioButtonToShowStoppedRecording() {
        btn_edit_add_audio.text = getString(R.string.label_tap_to_record)
    }
    private fun changeAddAudioButtonToShowRecording() {
        btn_edit_add_audio.text = getString(R.string.label_started_recording)
    }

    private fun showCancelledMessage() {
        showSnackbar(getString(R.string.label_cancelled))
    }

    private fun navigateToListFragment() {
        Navigation.findNavController(requireView()).popBackStack()
    }

    private fun showSavedMessage() {
        showSnackbar(getString(R.string.word_saved))
    }

    private fun showSnackBarForUndoDeletion() {
        val snackbar = Snackbar.make(requireView(), getString(R.string.label_pronunication_deleted), Snackbar.LENGTH_LONG)
        snackbar.setAction(getString(R.string.label_undo)) {
            viewModel.undoDeleteAudio()
        }
        snackbar.show()
    }

    private fun prepareUIForRecording() {
        btn_edit_audio.dontShow()
        btn_edit_add_audio.show()
    }

    /**
     * snackbar to show audio is playing
     */
    private fun showAudioPlayingMessage() {
        showSnackbar(getString(R.string.toast_playing_pro))
    }

    /**
     * when audio has been deleted or undoed, change button appearances
     */
    private fun changeAudioButtonAppearances(isDeleted : Boolean) {
        if (isDeleted){
            btn_edit_audio.text = getString(R.string.def_edit_word_audio_btn_label)
            btn_edit_audio.setIconResource(R.drawable.ic_add_black_24dp)

            //hide delete pronunciation btn
            btn_edit_delete_audio.dontShow()
        }else{
            //change btn text and icon if there's an audio associated with the word
            btn_edit_audio.text = getString(R.string.tap_to_listen_pro_edit_word)
            btn_edit_audio.setIconResource(R.drawable.ic_play_arrow_black_24dp)
            btn_edit_audio.show()

            //show delete pronunciation btn
            btn_edit_delete_audio.show()
        }
    }

    /**
     * set up the text fields and buttons
     * @param word the word fetched from db
     */
    private fun setUpUI(word: Word) {
        til_edit_word_name.editText!!.setText(word.name)
        til_edit_word_name.editText!!.requestFocus()

        if (word.meaning != null){
            til_edit_word_meaning.editText!!.setText(word.meaning)
        }else{
            til_edit_word_meaning.hint = getString(R.string.hint_meaning_dynamic)
        }

        if (word.audioPath != null){
            //change viewmodel audio present property
            viewModel.setIsAudioPresentProperty(true)
        }else {
            viewModel.setIsAudioPresentProperty(false)
        }
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        if (p1!!.action == MotionEvent.ACTION_DOWN){
            //press and hold to record audio
            viewModel.startRecording(tempAudioFileName)
            return true
        }
        else if (p1.action == MotionEvent.ACTION_UP){
            viewModel.stopRecording()
            return true
        }
        return false
    }


}
