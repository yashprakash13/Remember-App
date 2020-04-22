package save.newwords.vocab.remember.core.newword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import save.newwords.vocab.remember.repository.WordRepository

@Suppress("UNCHECKED_CAST")
class NewWordViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewWordViewModel::class.java)) {
            return NewWordViewModel(repository) as T
        }
        throw IllegalArgumentException("New VM Factory: Unknown ViewModel class")
    }


}