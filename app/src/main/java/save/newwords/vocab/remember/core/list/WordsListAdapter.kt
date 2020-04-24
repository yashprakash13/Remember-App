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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.each_word_item_view.view.*
import kotlinx.android.synthetic.main.each_word_item_view_grid.view.*
import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.common.dontShow
import save.newwords.vocab.remember.common.toggleVisibility
import save.newwords.vocab.remember.db.Word

class WordsListAdapter(private val context: Context, private val layoutManager: StaggeredGridLayoutManager, private val clickListener: (Word) -> Unit) :
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

        //item ids
        private var txtName: TextView = view.txt_word_text
        private var txtMean: TextView = view.txt_meaning
        private var imgBtnShowMean: ImageButton = view.imgbtn_show_meaning
        private var imgBtnHearPronunciation: ImageButton = view.imgbtn_hear_pronun

        fun bind (word: Word, clickListener: (Word) -> Unit){
            txtName.text = word.name

            //if meaning is null, don't show the show-meaning button
            if (word.meaning != null){
                txtMean.text = word.meaning
            }else {
                //if there's no meaning given, don't show the show-meaning button
                txtMean.dontShow()
                imgBtnShowMean.dontShow()
            }

            //if audio is null, don't show the play pronunciation button
            if (word.audioPath == null){
                imgBtnHearPronunciation.dontShow()
            }

            itemView.setOnClickListener{
                clickListener(word)
            }
            //to show and hide the meaning in list
            imgBtnShowMean.setOnClickListener {
                txtMean.toggleVisibility()
            }
        }
    }

    /**
     * The ViewHolder for Grid View
     */
    class WordItemViewHolderGrid(view: View): RecyclerView.ViewHolder(view){

        //item ids
        private var txtName: TextView = view.txt_word_text_grid
        private var txtMean: TextView = view.txt_word_mean_grid
        private var imgBtnHearPronunciation : ImageButton = view.imgbtn_hear_pronun_grid

        fun bind (word: Word, clickListener: (Word) -> Unit){
            txtName.text = word.name

            //if meaning is null, don't show the show-meaning button
            if (word.meaning != null){
                txtMean.text = word.meaning
            }else {
                txtMean.visibility = View.GONE
            }

            //if audio is null, don't show the play pronunciation button
            if (word.audioPath == null){
                imgBtnHearPronunciation.dontShow()
            }

            itemView.setOnClickListener{
                clickListener(word)
            }
        }
    }
}