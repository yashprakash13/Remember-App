package save.newwords.vocab.remember.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
import save.newwords.vocab.remember.common.AUDIO_PATH
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

    //root file
    private lateinit var root: File

    //word name received as argument from ListFrag upon item click
    private var wordNameClicked : String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_word, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init view model
        val factory = NewWordViewModelFactory(WordRepository(WordDatabase.getInstance(requireActivity())))
        viewModel = ViewModelProvider(this, factory).get(NewWordViewModel::class.java)

        //name through which the audio is saved temporarily in cache
        tempAudioFileName = "${requireActivity().externalCacheDir!!.absolutePath}/audiorecordtemp.3gp"

        //root file
        root = File(requireActivity().getExternalFilesDir("/"), AUDIO_PATH)

        /*
        Implementing tap and hold to record for mic button
         */
        imgbtn_record.setOnTouchListener(this)

        //observe if recording
        viewModel.isRecording.observe(viewLifecycleOwner, Observer {
            if (it){
                showRecordingMessage()
            }else{
                showRecordingStoppedMessage()
                changeToPlayIcon()
            }
        })

        //know if audio was recorded
        viewModel.isRecorded.observe(viewLifecycleOwner, Observer {
            if (it) {
                changeToMicIcon()
                showDeleteIcon()
            }
        })

        //observe if playing the recorded audio
        viewModel.isPlaying.observe(viewLifecycleOwner, Observer {
            if (it){
                showPlayingMessage()
            }else{
                showRecordingStoppedMessage()
            }
        })

        //observe if audio recording has exceeded the time limit
        viewModel.isTimeExceeded.observe(viewLifecycleOwner, Observer {
            if (it){
                viewModel.stopRecording()
                //this is extra to show the user that recording cannot exceed 5 seconds
                showTimeExceededSnackbar()
            }
        })

        //to delete the recorded audio
        imgbtn_delete_rec.setOnClickListener {
            txt_tap_to_record.text = getString(R.string.label_tap_to_record)
            imgbtn_delete_rec.visibility = View.GONE
            changeToMicIcon()
            viewModel.deleteCacheAudioRecording()
        }

        //to play the recorded audio back
        txt_tap_to_record.setOnClickListener {
            viewModel.playIfRecorded()
        }

        //if save button is clicked
        btn_save.setOnClickListener {
            if (!TextUtils.isEmpty(til_wordname.editText!!.text)){
                val word = Word(til_wordname.editText!!.text.toString().trim())
                if (!TextUtils.isEmpty((til_wordMeaning.editText!!.text))){
                    word.meaning = til_wordMeaning.editText!!.text.toString().trim()
                }
                //check if there is a recorded pronunciation file available
                if (viewModel.isRecorded.value!!){
                    //saves permanent file to disk and deletes the cache file
                    viewModel.getandSavePermanentFile(root, til_wordname.editText!!.text.toString().trim())
                    word.audioPath = til_wordname.editText!!.text.toString().trim() + ".3gp"
                }
                viewModel.saveWord(word)
                showWordSavedMessage()
                navigateToListFrag()
            }else{
                til_wordname.error = getString(R.string.error_word_name)
            }
        }

        //if delete button is clicked
        btn_delete.setOnClickListener {
            navigateToListFrag()
            showWordDiscardedMessage()
        }

    }

    private fun showTimeExceededSnackbar() {
        showSnackbar(getString(R.string.snack_max_audio_length))
    }

    private fun showWordDiscardedMessage() {
        makeToast(getString(R.string.toast_word_discarded))
    }

    private fun navigateToListFrag() {
        Navigation.findNavController(this.requireView()).popBackStack()
    }

    private fun showWordSavedMessage() {
        makeToast(getString(R.string.toast_word_saved))
    }

    private fun showDeleteIcon() {
        imgbtn_delete_rec.visibility = View.VISIBLE
    }

    private fun changeToMicIcon() {
        imgbtn_record.setImageDrawable(resources.getDrawable(R.drawable.ic_mic_black_24dp, null))
    }

    private fun changeToPlayIcon(){
        imgbtn_record.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp, null))

    }

    private fun showPlayingMessage() {
        txt_tap_to_record.text = getString(R.string.label_playing)
    }

    private fun showRecordingStoppedMessage() {
        txt_tap_to_record.text = getString(R.string.label_audio_recorded)
    }

    private fun showRecordingMessage() {
        txt_tap_to_record.text = getString(R.string.label_started_recording)
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
