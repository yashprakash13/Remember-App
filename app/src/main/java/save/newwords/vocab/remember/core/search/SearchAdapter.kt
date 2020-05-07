package save.newwords.vocab.remember.core.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.search_single_item.view.*
import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.core.list.WordItemDiffCallback
import save.newwords.vocab.remember.core.list.WordsListAdapter
import save.newwords.vocab.remember.db.Word

class SearchAdapter (private val context: Context,
                     private val clickListener: (Word) -> Unit,
                     private val wordList: List<Word>):
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {


    //inflate the single view for search item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_single_item, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int {
        return wordList.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(wordList[position], clickListener)
    }


    //the view holder class for search items
    inner class SearchViewHolder (itemView: View):  RecyclerView.ViewHolder(itemView){
        private var name: MaterialTextView = itemView.s_item_txt_name
        private var meaning: MaterialTextView = itemView.s_item_txt_meaning

        fun bind(word: Word, clicklistener: (Word) -> Unit){
            name.text = word.name
            meaning.text = word.meaning

            itemView.setOnClickListener {
                clicklistener(word)
            }
        }
    }

}