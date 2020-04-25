package save.newwords.vocab.remember.core.editword

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.repository.WordRepository

class EditWordViewModel (private val repository: WordRepository) : ViewModel() {

    private var viewmodelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewmodelJob)

    val clickedWordMutable  = MutableLiveData<Word>()

    fun getWordFromName(name : String) {
        uiScope.launch {
            clickedWordMutable.value = repository.getWordFromName(name)
        }
    }


}