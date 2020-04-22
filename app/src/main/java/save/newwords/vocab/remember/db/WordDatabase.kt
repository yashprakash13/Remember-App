package save.newwords.vocab.remember.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Word::class], version = 1, exportSchema = false)
abstract class WordDatabase : RoomDatabase() {

    abstract fun wordDao() : WordDao

    companion object{
        // For Singleton instantiation
        @Volatile private var INSTANCE: WordDatabase? = null

        fun getInstance(context: Context): WordDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WordDatabase::class.java,
                        "word_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
