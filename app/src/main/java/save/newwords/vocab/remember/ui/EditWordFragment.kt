package save.newwords.vocab.remember.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
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
import save.newwords.vocab.remember.common.AUDIO_PATH
import save.newwords.vocab.remember.common.dontShow
import save.newwords.vocab.remember.common.show
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
class EditWordFragment : Fragment() {

    //viewmodel instance
    private lateinit var viewModel: EditWordViewModel

    //navigation argument
    private val args: EditWordFragmentArgs by navArgs()

    //local storage root
    private lateinit var root: File

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
            }else{
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
            if (!TextUtils.isEmpty(til_edit_word_name.editText!!.text.toString().trim())){
                viewModel.clickedWordMutable.value!!.name = til_edit_word_name.editText!!.text.toString().trim()
                //if meaning field is not empty
                if (!TextUtils.isEmpty(til_edit_word_meaning.editText!!.text.toString().trim())){
                    viewModel.clickedWordMutable.value!!.meaning = til_edit_word_meaning.editText!!.text.toString().trim()
                }
                //TODO: implement audio save actions
                //viewModel.updateWord(wordNameClicked)
                showSavedMessage()
                navigateToListFragment()

            }else{
                //if the name til is blank
                til_edit_word_name.error = getString(R.string.label_error_word_without_name)
            }
        }

        //------------------------------audio recording functionalities-----------------------------




        //------------------------------------------------------------------------------------------





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
        snackbar.setAction("Undo") {
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


}
