package save.newwords.vocab.remember.repository

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.db.WordDatabase

class WordRepository(private val database: WordDatabase) {

    fun getAllWordsRoom(): LiveData<List<Word>> = database.wordDao().getAllWords()

    fun getAllWordsRoomPaged() : DataSource.Factory<Int, Word>
            = database.wordDao().getAllWordsPaged()

    fun getAllWordsRoomPagedAlphabetically() : DataSource.Factory<Int, Word>
            = database.wordDao().getAllWordsPagedAlphabetically()

    fun saveWordToDb(word: Word){
        CoroutineScope(Dispatchers.IO).launch {
            database.wordDao().insertWord(word)
        }
    }

    fun deleteWordFromDb(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.wordDao().deleteWord(name)
        }
    }

    suspend fun getWordFromName(name: String) : Word {
        return withContext(Dispatchers.IO){
            database.wordDao().getWordFromName(name)
        }
    }

    fun updateWordInDb(originalName: String, word: Word){
        CoroutineScope(Dispatchers.IO).launch {
            database.wordDao().updateWord(originalName, word.name, word.meaning, word.audioPath)
        }
    }

    fun searchFromDb(searchString: String) : LiveData<List<Word>> {
        return database.wordDao().searchWords(searchString)
    }

    //TODO implement other methods

}