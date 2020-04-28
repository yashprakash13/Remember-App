package save.newwords.vocab.remember.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.app_bar_main_bottom_sheet.*
import kotlinx.android.synthetic.main.each_word_item_view.*
import kotlinx.android.synthetic.main.each_word_item_view.view.*
import kotlinx.android.synthetic.main.each_word_item_view.view.imgbtn_hear_pronun
import kotlinx.android.synthetic.main.each_word_item_view_grid.*
import kotlinx.android.synthetic.main.each_word_item_view_grid.view.*
import kotlinx.android.synthetic.main.each_word_item_view_grid.view.imgbtn_hear_pronun_grid
import kotlinx.android.synthetic.main.fragment_list.*

import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.common.*
import save.newwords.vocab.remember.core.list.WordsListAdapter
import save.newwords.vocab.remember.core.list.WordsListViewModel
import save.newwords.vocab.remember.core.list.WordsListViewModelFactory
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.db.WordDatabase
import save.newwords.vocab.remember.repository.WordRepository
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class ListFragment : Fragment(), (Word, Int) -> Unit {

    //view model instance for list fragment
    private lateinit var viewModel: WordsListViewModel

    //adapter instance
    private lateinit var adapter: WordsListAdapter

    //layout mananger instance
    private lateinit var layoutManager: StaggeredGridLayoutManager

    //root file
    private lateinit var root: File


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    /**
     * It is necessary to set setHasOptionsMenu(true) for enabling menus in Fragment
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set the bottom app bar
        (requireActivity() as AppCompatActivity).setSupportActionBar(bottom_app_bar)

        //root file
        root = File(requireActivity().getExternalFilesDir("/"), AUDIO_PATH)

        /*
        attach layoutmanager to recyclerview
         */
        setLayoutForRecyclerView()

        //init adapter and attach to recyclerview list
        adapter = WordsListAdapter(requireActivity(), layoutManager, this)
        recy_words_list.adapter = adapter

        //init view model according to the sort by pref
        setUpViewModelListObserver()

        //enable left swipe on recyclerview items with ItemTouchHelper object
        enableSwipe()

        //on new word fab button clicked
        fab_new.setOnClickListener{
            navigateToNewWordFrag()
        }

        //for the bottom-app-bar bottomsheet through navigation icon
        setUpBottomSheetNavigation()
    }


    /**
     * the clicklistener for word clicked, or the hear pronunciation btn clicked
     * @param wordClicked: The word clicked on the list
     * @param clickType: the type of click: can be either of two:
     * @see WORD_CLICKED and
     * @see WORD_PRONUNCIATION_BTN_CLICKED
     */
    override fun invoke(wordClicked: Word, clickType: Int) {
        when(clickType){
            WORD_CLICKED -> {
                navigateToEditWordFragment(wordClicked.name)
            }
            WORD_PRONUNCIATION_BTN_CLICKED -> {
                val root = File(requireActivity().getExternalFilesDir("/"), AUDIO_PATH)
                viewModel.hearPronunciation(root, wordClicked)
                showSnackbar(getString(R.string.toast_playing_pro))
            }
        }
    }

    private fun navigateToEditWordFragment(name: String) {
        val action = ListFragmentDirections.actionListFragmentToEditWordFragment(name)
        Navigation.findNavController(requireView()).navigate(action)
    }

    /**
     * to change the sort by preference
     * @param changeTo : the integer value(recent or alpha) to change to
     */
    private fun changeSortByPreference(changeTo: Int){
        val preferences = activity?.getSharedPrefsFor(R.string.sort_by_pref_key)
        val defValue = resources.getInteger(R.integer.list_sort_pref_recent)

        val currentValue = preferences!!.getInt(getString(R.string.sort_by_pref_key), defValue)

        if ((currentValue != changeTo) && (changeTo == resources.getInteger(R.integer.list_sort_pref_recent))){
            activity?.changeSharedPrefTo(
                preferences, R.string.sort_by_pref_key,
                resources.getInteger(R.integer.list_sort_pref_recent),
                resources.getInteger(R.integer.pref_is_of_int_type))
            refreshActivity()
        }else if ((currentValue != changeTo) && (changeTo == resources.getInteger(R.integer.list_sort_pref_alpha))){
            activity?.changeSharedPrefTo(
                preferences, R.string.sort_by_pref_key,
                resources.getInteger(R.integer.list_sort_pref_alpha),
                resources.getInteger(R.integer.pref_is_of_int_type))
            refreshActivity()

        }
    }

    /**
     * refresh activity for displaying the new sorted list
     */
    private fun refreshActivity() {
        val i = Intent(this.activity, MainActivity::class.java)
        this.activity?.finish()
        startActivity(i)
    }

    /**
     * set up the view model acc to shared pref sort by
     */
    private fun setUpViewModelListObserver(){
        val preferences = activity?.getSharedPrefsFor(R.string.sort_by_pref_key)
        val defValue = resources.getInteger(R.integer.list_sort_pref_recent)

        val currentValue = preferences!!.getInt(getString(R.string.sort_by_pref_key), defValue)

        if (currentValue == resources.getInteger(R.integer.list_sort_pref_recent)){
            setUpRecentWordsPagedList()
        }else{
            setUpAlphaWordsPagedList()
        }
    }

    /**
     * helper function to
     * set up observer on pagedlist
     * sort by alphabetically
     */
    private fun setUpAlphaWordsPagedList() {
        val factory = WordsListViewModelFactory(WordRepository((WordDatabase.getInstance(requireActivity()))), 1)
        viewModel = ViewModelProvider(this, factory).get(WordsListViewModel::class.java)

        viewModel.getWordsPagedList().observe(viewLifecycleOwner, Observer {pagedList ->
            if (pagedList != null){
                adapter.submitList(pagedList)
            }
            //TODO: Show empty recyclerview text/image in the else condition
        })
    }
    /**
     * helper function to
     * set up observer on pagedlist
     * sort by recent words
     */
    private fun setUpRecentWordsPagedList() {
        val factory = WordsListViewModelFactory(WordRepository((WordDatabase.getInstance(requireActivity()))), 0)
        viewModel = ViewModelProvider(this, factory).get(WordsListViewModel::class.java)

        viewModel.getWordsPagedList().observe(viewLifecycleOwner, Observer {pagedList ->
            if (pagedList != null){
                adapter.submitList(pagedList)
            }
            //TODO: Show empty recyclerview text/image in the else condition
        })
    }


    /**
     * Nav Bottom Sheet functionalities implemented
     */
    private fun setUpBottomSheetNavigation() {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_main_app_bar)
        //originally, it should be hidden
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        //expand on nav btn click
        bottom_app_bar.setNavigationOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        //close on close btn click
        imgbtn_close_bottom_sheet_main.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        /**
         * Click listeners for the items in the bottom sheet
         */
        lin_sort_by_recent_words.setOnClickListener {
            changeSortByPreference(resources.getInteger(R.integer.list_sort_pref_recent))
        }

        lin_sort_by_alpha_words.setOnClickListener {
            changeSortByPreference(resources.getInteger(R.integer.list_sort_pref_alpha))
        }

        lin_gotosettings.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            navigateToOptionsFrag()
        }
    }

    private fun navigateToOptionsFrag() {
        Navigation.findNavController(requireView()).navigate(R.id.action_listFragment_to_optionsFragment)
    }


    /**
     * Change layout spancount of recyclerview based on option selected from sharedprefs
     * and the number of columns got from screen width using
     * @see getNumColumns function
     */
    private fun changeLayoutForRecyclerview() {
        if (layoutManager.spanCount == 1){
            val numCols = requireContext().getNumColumns()
            layoutManager.spanCount = numCols
        } else{
            layoutManager.spanCount = 1
        }
    }


    /**
     * Set Layout for recyclerview based on sharedpref saved value
     * set number of columns dynamically by
     * calculating the width of screen through
     * helper function
     * @see getNumColumns function
     */
    private fun setLayoutForRecyclerView() {
        val prefLayoutManager = activity?.getSharedPreferences(getString(R.string.layout_pref_key), Context.MODE_PRIVATE)
        val defValue = resources.getInteger(R.integer.layout_pref_linear)
        val prefLayout = prefLayoutManager!!.getInt(getString(R.string.layout_pref_key), defValue)

        layoutManager = if (prefLayout == resources.getInteger(R.integer.layout_pref_linear)) {
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        }else{
            val numCols = requireContext().getNumColumns()
            StaggeredGridLayoutManager(numCols, StaggeredGridLayoutManager.VERTICAL)
        }
        recy_words_list.layoutManager = layoutManager
    }


    /**
     * helper function to navigate to new word fragment
     */
    private fun navigateToNewWordFrag() {
        Navigation.findNavController(this.requireView()).navigate(R.id.action_listFragment_to_newWordFragment)
    }


    /**
     * Overidden method for menu inflation through bottom app bar
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bottom_app_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Overrriden method for menu selection options through bottom app bar
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_change_layout -> {
                changeSharedPrefsForLayout()
                changeLayoutForRecyclerview()
                notifyAdapterAboutLayoutChange()
            }
        }
        return true
    }


    /**
     * Adapter needs to be notified when the layout manager is changed
     */
    private fun notifyAdapterAboutLayoutChange() {
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }


    /**
     * Change the value of shared pref when the layout change button is clicked
     * @see getSharedPrefsFor function for getting SP instance
     * @see changeSharedPrefTo function for changing SP instance
     */
    private fun changeSharedPrefsForLayout() {
        val prefLayoutManager = activity?.getSharedPrefsFor(R.string.layout_pref_key)
        val defValue = resources.getInteger(R.integer.layout_pref_linear)

        //current value of pref
        val prefLayout = prefLayoutManager!!.getInt(getString(R.string.layout_pref_key), defValue)

        if (prefLayout == resources.getInteger(R.integer.layout_pref_grid)) {
                activity?.changeSharedPrefTo(prefLayoutManager, R.string.layout_pref_key,
                resources.getInteger(R.integer.layout_pref_linear), resources.getInteger(R.integer.pref_is_of_int_type))
        }else{
            activity?.changeSharedPrefTo(prefLayoutManager, R.string.layout_pref_key,
                resources.getInteger(R.integer.layout_pref_grid), resources.getInteger(R.integer.pref_is_of_int_type))
        }
    }


    /**
     * Helper function to delete swiped word
     */
    fun deleteWord(name: String){
        viewModel.deleteWord(name)

    }


    /**
     * Helper function to restore/undo swipe deleted word
     */
    fun restoreWord(name: String){
        viewModel.restoreDeletedWord()
    }


    /**
     * Helper function to enable swipe functionality for recyclerview items
     */
    private fun enableSwipe() {
        val p = Paint()
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val wordNameToDelete : String = when(viewHolder){
                        is WordsListAdapter.WordItemViewHolder -> viewHolder.itemView.txt_word_text.text.toString()
                        else -> viewHolder.itemView.txt_word_text_grid.text.toString()
                    }
                    //delete word
                    deleteWord(wordNameToDelete)


                    //show snackbar
                    val snackbar = Snackbar.make(requireView(), " Word deleted!", Snackbar.LENGTH_LONG)
                    snackbar.setAction("UNDO") {
                        // restore deleted word
                        restoreWord(wordNameToDelete)
                    }
                    snackbar.addCallback(object : Snackbar.Callback(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            viewModel.deleteAudioForWord(root)
                        }
                    })
                    snackbar.setActionTextColor(Color.WHITE)
                    snackbar.show()

                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val drawableIcon : Drawable = resources.getDrawable(R.drawable.ic_delete_outline_white_24dp, null)
                    val icon = drawableToBitmap(drawableIcon)

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                        val itemView = viewHolder.itemView
                        val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                        val width = height / 3

                            p.color = Color.parseColor("#F65C47")
                            val background = RectF(
                                itemView.right.toFloat() + dX,
                                itemView.top.toFloat(),
                                itemView.right.toFloat(),
                                itemView.bottom.toFloat()
                            )
                            c.drawRect(background, p)
                            val iconDest = RectF(
                                itemView.right.toFloat() - 2 * width,
                                itemView.top.toFloat() + width,
                                itemView.right.toFloat() - width,
                                itemView.bottom.toFloat() - width
                            )
                            c.drawBitmap(icon!!, null, iconDest, p)
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recy_words_list)
    }

}
