package save.newwords.vocab.remember.core.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.each_word_item_view.view.*
import kotlinx.android.synthetic.main.each_word_item_view_grid.view.*
import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.common.dontShow
import save.newwords.vocab.remember.common.toggleVisibility
import save.newwords.vocab.remember.db.Word

class WordsListAdapter(private val context: Context, private val layoutManager: GridLayoutManager, private val clickListener: (Word) -> Unit) :
    PagedListAdapter<Word, RecyclerView.ViewHolder>(WordItemDiffCallback()) {

    enum class ViewType {
        LINEAR,
        GRID
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            ViewType.LINEAR.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.each_word_item_view, parent, false)
                WordItemViewHolder(view)
            }
            else ->{
                val view = LayoutInflater.from(context).inflate(R.layout.each_word_item_view_grid, parent, false)
                WordItemViewHolderGrid(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder){
            is WordItemViewHolder -> holder.bind(this.getItem(position)!!, clickListener)
            is WordItemViewHolderGrid -> holder.bind(this.getItem(position)!!, clickListener)
        }
    }



    override fun getItemViewType(position: Int): Int {
        return if (layoutManager.spanCount == 1) ViewType.LINEAR.ordinal
        else ViewType.GRID.ordinal
    }

    /**
     * The ViewHolder for List view
     */
    class WordItemViewHolder(view: View): RecyclerView.ViewHolder(view){

        private var txtName: TextView = view.txt_word_text
        private var txtMean: TextView = view.txt_meaning
        private var imgBtnShowMean: ImageButton = view.imgbtn_show_meaning

        fun bind (word: Word, clickListener: (Word) -> Unit){
            txtName.text = word.name
            if (word.meaning != null){
                txtMean.text = word.meaning
            }else {
                //if there's no meaning given, don't show the show-meaning button
                txtMean.dontShow()
                imgBtnShowMean.dontShow()
            }

            itemView.setOnClickListener{
                clickListener(word)
            }
            imgBtnShowMean.setOnClickListener {
                txtMean.toggleVisibility()

            }
        }
    }

    /**
     * The ViewHolder for Grid View
     */
    class WordItemViewHolderGrid(view: View): RecyclerView.ViewHolder(view){

        private var txtName: TextView = view.txt_word_text_grid
        private var txtMean: TextView = view.txt_word_mean_grid

        fun bind (word: Word, clickListener: (Word) -> Unit){
            txtName.text = word.name
            if (word.meaning != null){
                txtMean.text = word.meaning
            }else {
                txtMean.visibility = View.GONE
            }

            itemView.setOnClickListener{
                clickListener(word)
            }
        }
    }
}