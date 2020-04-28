package save.newwords.vocab.remember.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_new_word.*

import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.common.*
import save.newwords.vocab.remember.common.isValid
import save.newwords.vocab.remember.common.makeToast
import save.newwords.vocab.remember.common.showSnackbar
import save.newwords.vocab.remember.core.newword.NewWordViewModel
import save.newwords.vocab.remember.core.newword.NewWordViewModelFactory
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.db.WordDatabase
import save.newwords.vocab.remember.repository.WordRepository
import java.io.File

class NewWordFragment : Fragment(), View.OnTouchListener {

    //for name of audio string
    private lateinit var tempAudioFileName: String

    //viewmodel instance
    private lateinit var viewModel: NewWordViewModel

    //root file in local storage
    private lateinit var root: File


    /**
     * inflate xml file
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_word, container, false)
    }

    /**
     * for all other tasks
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init view model
        val factory = NewWordViewModelFactory(WordRepository(WordDatabase.getInstance(requireActivity())))
        viewModel = ViewModelProvider(this, factory).get(NewWordViewModel::class.java)

        //name through which the audio is saved temporarily in cache
        tempAudioFileName = "${requireActivity().externalCacheDir!!.absolutePath}/audiorecordtemp.3gp"

        //root file
        root = File(requireActivity().getExternalFilesDir("/"), AUDIO_PATH)

        //Implementing tap and hold to record for audio
        btn_new_add_audio.setOnTouchListener(this)

        //when add audio btn is clicked, show the tap to record button and hide this button
        btn_new_audio.setOnClickListener {
            //if audio has been recorded, play it back
            if (viewModel.isRecorded.value!!){
                viewModel.playIfRecorded()
            }else {
                //otherwise, show the add audio button
                changeUIToAddAudio()
            }
        }

        //observe if recording
        viewModel.isRecording.observe(viewLifecycleOwner, Observer {
            if (it){
                showRecordingMessage()
            }
        })

        //know if audio was recorded
        viewModel.isRecorded.observe(viewLifecycleOwner, Observer {
            if (it) {
                changeAudioButtonAppearances(true)
            }
        })

        //observe if playing the recorded audio
        viewModel.isPlaying.observe(viewLifecycleOwner, Observer {
            if (it){
                showPlayingMessage()
            }
        })

        //observe if audio recording has exceeded the time limit
        viewModel.isTimeExceeded.observe(viewLifecycleOwner, Observer {
            if (it){
                viewModel.stopRecording()
                //to show the user that recording cannot exceed 5 seconds
                showTimeExceededSnackbar()
            }
        })

        //to delete the recorded audio
        btn_add_delete_audio.setOnClickListener {
            changeAudioButtonAppearances(false)

            //delete audio saved in cache
            viewModel.deleteCacheAudioRecording()
        }

        //if save button is clicked, save the word to db
        btn_save.setOnClickListener {
            if (til_wordname.isValid()){
                val word = Word(til_wordname.getString())
                if (til_wordMeaning.isValid()){
                    word.meaning = til_wordMeaning.getString()
                }
                //check if there is a recorded pronunciation file available
                if (viewModel.isRecorded.value!!){
                    //saves permanent file to disk and deletes the cache file
                    viewModel.getAndSavePermanentFile(root, til_wordname.getString())
                    word.audioPath = til_wordname.getString() + ".3gp"
                }
                viewModel.saveWord(word)
                showWordSavedMessage()
                navigateToListFrag()
            }else{
                til_wordname.error = getString(R.string.error_word_name)
            }
        }

        //if delete/cancel button is clicked, discard the word
        btn_delete.setOnClickListener {
            navigateToListFrag()
            showWordDiscardedMessage()
        }

    }

    private fun changeUIToAddAudio() {
        btn_new_audio.dontShow()
        btn_new_add_audio.show()
    }

    private fun showTimeExceededSnackbar() {
        showSnackbar(getString(R.string.snack_max_audio_length))
    }

    private fun showWordDiscardedMessage() {
        showSnackbar(getString(R.string.toast_word_discarded))
    }

    private fun navigateToListFrag() {
        Navigation.findNavController(requireView()).popBackStack()
    }

    private fun showWordSavedMessage() {
        showSnackbar(getString(R.string.toast_word_saved))
    }

    private fun showPlayingMessage() {
        showSnackbar(getString(R.string.toast_playing_pro))
    }

    private fun changeAudioButtonAppearances(isAudioRecorded: Boolean) {
        //when audio has been recorded, show the delete button, change text of audio button
        //and hide the add audio button
        if (isAudioRecorded){
            btn_new_audio.text = getString(R.string.tap_to_listen_pro_edit_word)
            btn_new_audio.show()
            btn_new_add_audio.dontShow()
            btn_add_delete_audio.show()
        }else{
            //if audio was deleted, hide the delete button, change text of add audio button to its
            //default text and change text of audio button to its default too
            btn_new_audio.text = getString(R.string.def_edit_word_audio_btn_label)
            btn_add_delete_audio.dontShow()
            btn_new_add_audio.text = getString(R.string.label_tap_to_record)
        }
    }

    private fun showRecordingMessage() {
        btn_new_add_audio.text = getString(R.string.label_started_recording)
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
