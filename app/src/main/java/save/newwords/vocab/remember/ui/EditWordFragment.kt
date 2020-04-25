package save.newwords.vocab.remember.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_edit_word.*
import kotlinx.android.synthetic.main.fragment_new_word.*
import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.common.show
import save.newwords.vocab.remember.common.showSnackbar
import save.newwords.vocab.remember.core.editword.EditWordViewModel
import save.newwords.vocab.remember.core.editword.EditWordViewModelFactory
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.db.WordDatabase
import save.newwords.vocab.remember.repository.WordRepository

/**
 * A simple [Fragment] subclass.
 */
class EditWordFragment : Fragment() {

    //viewmodel instance
    private lateinit var viewModel: EditWordViewModel

    //word clicked instance
    private lateinit var wordClicked: Word

    //navigation argument
    private val args: EditWordFragmentArgs by navArgs()

    /**
     * inflate xml view
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

        //init viewmodel instance
        val factory = EditWordViewModelFactory(WordRepository(WordDatabase.getInstance(requireActivity())))
        viewModel =  ViewModelProvider(this, factory).get(EditWordViewModel::class.java)

        //get the word from db from its name
        viewModel.getWordFromName(wordNameClicked)

        viewModel.clickedWordMutable.observe(viewLifecycleOwner, Observer {
            if (it != null){
                //meaning word has been fetched from db
                setUpUI(it)
                wordClicked = it
            }
        })



    }

    private fun setUpUI(word: Word) {
        til_edit_word_name.editText!!.setText(word.name)
        til_edit_word_name.editText!!.requestFocus()

        if (word.meaning != null){
            til_edit_word_meaning.editText!!.setText(word.meaning)
        }else{
            til_edit_word_meaning.hint = getString(R.string.hint_meaning_dynamic)
        }

        if (word.audioPath != null){
            //change btn text and icon if there's an audio associated with the word
            btn_edit_audio.text = getString(R.string.tap_to_listen_pro_edit_word)
            btn_edit_audio.setIconResource(R.drawable.ic_play_arrow_black_24dp)

            //show delete pronunciation btn
            btn_edit_delete_audio.show()
        }
    }


}
