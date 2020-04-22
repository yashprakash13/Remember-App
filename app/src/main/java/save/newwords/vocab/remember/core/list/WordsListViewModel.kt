package save.newwords.vocab.remember.core.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.*
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.repository.WordRepository

class WordsListViewModel(private val repository: WordRepository): ViewModel() {

    //scope to perform db functions in viewmodel
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //pagedlist of all words to display in list
    private var wordsLiveData : LiveData<PagedList<Word>>

    //mutable so that word can be assigned when the runBlocking coroutine completes
    private val storeDeletedWord = MutableLiveData<Word>()



    init {
        val factory: DataSource.Factory<Int, Word> = repository.getAllWordsRoomPaged()
        val pagedListBuilder = LivePagedListBuilder<Int, Word>(factory, 10)
        wordsLiveData = pagedListBuilder.build()
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
     * restore deleted word in case undo is clicked
     */
    fun restoreDeletedWord() {
        uiScope.launch {
            storeDeletedWord.value?.let { repository.saveWordToDb(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    //TODO Implement other methods

}