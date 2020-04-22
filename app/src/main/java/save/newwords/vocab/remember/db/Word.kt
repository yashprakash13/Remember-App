package save.newwords.vocab.remember.db

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_table")
data class Word (
    @PrimaryKey
    @NonNull
    var name: String,
    var meaning: String? = null,
    var audioPath: String? = null

)