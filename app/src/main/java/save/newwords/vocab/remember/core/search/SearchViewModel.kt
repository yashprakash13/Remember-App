package save.newwords.vocab.remember.core.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.repository.WordRepository

class SearchViewModel(private val repository: WordRepository) : ViewModel() {

    /**
     * to get the searched words from the db
     * @param searchString the string containing a part of name or meaning to be searched
     */
    fun getSearchedWords(searchString: String) : LiveData<List<Word>> {
        return repository.searchFromDb(searchString)
    }

}





