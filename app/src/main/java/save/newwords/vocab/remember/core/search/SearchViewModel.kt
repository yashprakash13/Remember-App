package save.newwords.vocab.remember.core.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.repository.WordRepository

class SearchViewModel(private val repository: WordRepository) : ViewModel() {

    //scope to perform db functions in viewmodel
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //word list
    lateinit var wordLiveData : LiveData<List<Word>>

    fun getSearchedWords(searchString: String) : LiveData<List<Word>> {
        return repository.searchFromDb(searchString)
    }



}





