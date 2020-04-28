package save.newwords.vocab.remember.core.list

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.*
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.repository.WordRepository
import java.io.File
import java.io.IOException

class WordsListViewModel(private val repository: WordRepository, private val sortBy: Int): ViewModel() {

    //scope to perform db functions in viewmodel
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //pagedlist of all words to display in list
    private var wordsLiveData : LiveData<PagedList<Word>>

    //mutable so that word can be assigned when the runBlocking coroutine completes
    private val storeDeletedWord = MutableLiveData<Word>()

    //to play audio from local storage
    private var player: MediaPlayer? = null

    //to track if the word will be deleted or not
    //if undo is clicked, this will turn false
    //otherwise it will remain true
    private val isAudioToBeDeleted = MutableLiveData<Boolean> ()



    init {
        /**
         * 0 means recent words, 1 means by alphabetical order
         */
        wordsLiveData = if (sortBy == 0) {
            val factory: DataSource.Factory<Int, Word> = repository.getAllWordsRoomPaged()
            val pagedListBuilder = LivePagedListBuilder<Int, Word>(factory, 10)
            pagedListBuilder.build()
        }else{
            val factoryAlpha: DataSource.Factory<Int, Word> = repository.getAllWordsRoomPagedAlphabetically()
            val pagedListBuilderAlpha = LivePagedListBuilder<Int, Word>(factoryAlpha, 10)
            pagedListBuilderAlpha.build()
        }
    }

    /**
     * get the words list from db as a paged list in batches of 10
     */
    fun getWordsPagedList() = wordsLiveData


    /**
     * Store word to be deleted and then delete the word from db
     */
    fun deleteWord(name: String) {
        storeJustDeletedWord(name)
        uiScope.launch {
            repository.deleteWordFromDb(name)
        }
        //the word needs to be deleted
        isAudioToBeDeleted.value = true
    }

    /**
     * Store deleted word first through runBlocking
     */
    private fun storeJustDeletedWord(name: String) {
        storeDeletedWord.value = runBlocking {
            repository.getWordFromName(name)
        }
    }

    /**
     * to restore deleted word in case undo is clicked
     */
    fun restoreDeletedWord() {
        uiScope.launch {
            storeDeletedWord.value?.let { repository.saveWordToDb(it) }
        }
        //the undo button on snackbar was clicked
        isAudioToBeDeleted.value = false
    }

    /**
     * to delete audio permanently from local storage
     */
    fun deleteAudioForWord (root: File){
        //if there's an audio associated with the word and the undo button was not clicked
        if (storeDeletedWord.value!!.audioPath != null && isAudioToBeDeleted.value!!) {
            val fileToDelete = File(root, storeDeletedWord.value!!.audioPath!!)
            fileToDelete.delete()
        }
    }

    /**
     * to hear pronunciation from clicked word in the list
     * @param root: the internal storage path
     */
    fun hearPronunciation(root: File, word: Word) {
        val audioFilePath = File(root, "${word.name}.3gp").absolutePath
        player = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("AUDIO PLAYING ERROR: ", "prepare() failed")
            }
        }

        player!!.setOnCompletionListener {
            //do something
        }
    }




    /**
     * To cancel the coroutine jobs when view model is deleted upon activity close
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}