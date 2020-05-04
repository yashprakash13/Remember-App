package save.newwords.vocab.remember.core.options

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import save.newwords.vocab.remember.core.newword.NewWordViewModel

@Suppress("UNCHECKED_CAST")
class OptionsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OptionsViewModel::class.java)) {
            return OptionsViewModel(context) as T
        }
        throw IllegalArgumentException("New VM Factory: Unknown ViewModel class")
    }
}