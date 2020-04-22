package save.newwords.vocab.remember.core.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import save.newwords.vocab.remember.repository.WordRepository

@Suppress("UNCHECKED_CAST")
class WordsListViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordsListViewModel::class.java)){
            return WordsListViewModel(repository) as T
        }
        throw IllegalArgumentException("List VM Factory: Unknown ViewModel class")
    }
}