package save.newwords.vocab.remember.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_search.*

import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.core.search.SearchAdapter
import save.newwords.vocab.remember.core.search.SearchViewModel
import save.newwords.vocab.remember.core.search.SearchViewModelFactory
import save.newwords.vocab.remember.db.Word
import save.newwords.vocab.remember.db.WordDatabase
import save.newwords.vocab.remember.repository.WordRepository

class SearchFragment : Fragment(), (Word) -> Unit {

    //viewmodel instance
    private lateinit var viewModel: SearchViewModel
    //adapter instance
    private lateinit var adapter: SearchAdapter

    /**
     * inflate XML layout for fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init viewmodel instance
        val factory = SearchViewModelFactory(
            WordRepository(WordDatabase.getInstance(requireActivity())))
        viewModel = ViewModelProvider(this, factory).get(SearchViewModel::class.java)

        //init recyclerview
        search_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        search_recycler_view.addItemDecoration(DividerItemDecoration(context,
            LinearLayoutManager.VERTICAL))

        /**
         * to get the search items from db as a word name
         * or meaning is typed or on submit
         */
        search_search_view.requestFocus()
        search_search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                getItemsFromDb(p0)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                getItemsFromDb(p0)
                return true
            }

        })
    }

    /**
     * to get the live items from db and display in adapter
     */
    private fun getItemsFromDb(string: String?) {
        //make sure the search string can be searched in between texts, at the start and at the end
        val searchText = "%$string%"
        viewModel.getSearchedWords(searchText).observe(viewLifecycleOwner, Observer {
            //init adapter here, so that new data is shown everytime something new is typed
            adapter = SearchAdapter(requireContext(), this, it)
            search_recycler_view.adapter = adapter
        })
    }

    /**
     * the clicklistener for word clicked in search list
     * @param wordClicked for sending the name to edit fragment
     */
    override fun invoke(wordClicked: Word) {
        val action = SearchFragmentDirections.
            actionSearchFragmentToEditWordFragment(wordClicked.name)
        Navigation.findNavController(requireView()).navigate(action)
    }

}
