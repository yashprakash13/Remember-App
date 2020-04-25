package save.newwords.vocab.remember.core.editword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import save.newwords.vocab.remember.repository.WordRepository

@Suppress("UNCHECKED_CAST")
class EditWordViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditWordViewModel::class.java)){
            return EditWordViewModel(repository) as T
        }
        throw IllegalArgumentException("List VM Factory: Unknown ViewModel class")
    }
}