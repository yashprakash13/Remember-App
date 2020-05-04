package save.newwords.vocab.remember.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWord(word: Word)

    @Query("Select * from word_table where name like :name limit 1")
    fun getWordFromName (name: String) : Word

    @Query("Update word_table set name = :name, meaning = :meaning, audioPath = :audiopath where name like :originalName")
    fun updateWord(originalName: String, name: String, meaning:String?, audiopath: String?)

    @Query("Select * from word_table where name like :s_name or meaning like :s_meaning")
    fun searchWords(s_name: String, s_meaning: String) : LiveData<List<Word>>

}