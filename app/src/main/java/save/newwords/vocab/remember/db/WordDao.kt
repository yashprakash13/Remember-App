package save.newwords.vocab.remember.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WordDao {

    @Query ("Select * from word_table")
    fun getAllWords() : LiveData<List<Word>>

    @Query ("Select * from word_table")
    fun getAllWordsPaged() : DataSource.Factory<Int, Word>

    @Query("Select * from word_table order by name")
    fun getAllWordsPagedAlphabetically() : DataSource.Factory<Int, Word>

    @Query("Delete from word_table where name like :name")
    fun deleteWord(name: String)

    @Insert
    fun insertWord(word: Word)

    @Query("Select * from word_table where name like :name limit 1")
    fun getWordFromName (name: String) : Word

//    @Query("Select * from word_table where name like :s_name or meaning like :s_meaning")
//    fun searchWords(s_name: String, s_meaning: String)

}