package save.newwords.vocab.remember.core.list

import androidx.recyclerview.widget.DiffUtil
import save.newwords.vocab.remember.db.Word

class WordItemDiffCallback : DiffUtil.ItemCallback<Word>() {

    override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem == newItem
    }
}