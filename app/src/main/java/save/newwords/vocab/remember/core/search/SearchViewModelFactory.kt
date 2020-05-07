package save.newwords.vocab.remember.core.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import save.newwords.vocab.remember.repository.WordRepository
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)){
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("New VM Factory: Unknown ViewModel class")
    }
}