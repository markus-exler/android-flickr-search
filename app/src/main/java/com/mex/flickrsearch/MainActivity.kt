package com.mex.flickrsearch

import android.app.SearchManager
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import com.mex.flickrsearch.data.SearchHistoryProvider
import com.mex.flickrsearch.data.SearchHistoryProvider.Companion.AUTHORITY
import com.mex.flickrsearch.databinding.ActivityMainBinding
import com.mex.flickrsearch.searchview.PictureGridAdapter
import com.mex.flickrsearch.searchview.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener,
    SearchView.OnSuggestionListener {

    private lateinit var searchMenuItem: MenuItem
    private lateinit var searchView: SearchView
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var adapter: PictureGridAdapter
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initListAdapter()
        initCurrentQueryObserver()
        binding.btRetry.setOnClickListener { adapter.retry() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        // set up the search view in the action bar
        val manager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.search)
        searchView = searchMenuItem.actionView as SearchView
        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(this)
        searchView.setOnSuggestionListener(this)

        // workaround, so that the suggestion dropdown does not cover the searchView when the keyboard is shown
        val searchText =
            searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text) as? AutoCompleteTextView
        searchView.setOnSearchClickListener {
            searchText?.dismissDropDown()
            lifecycleScope.launch {
                delay(500)
                searchText?.showDropDown()
            }
        }
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        // search for the input query
        searchPicture(query)
        return true
    }

    override fun onSuggestionClick(position: Int): Boolean {
        // get the clicked suggestion text and search for the text
        val cursor = searchView.suggestionsAdapter.cursor
        cursor.moveToPosition(position)
        searchPicture(cursor.getString(2))
        return true
    }

    override fun onQueryTextChange(s: String?): Boolean {
        return false
    }

    override fun onSuggestionSelect(position: Int): Boolean {
        return false
    }

    private fun initCurrentQueryObserver(){
        viewModel.currentQuery.observe(this,{
            supportActionBar?.subtitle = it
        })
    }

    /**
     * Initiate the list adapter and load state listener to display the load state and errors in the UI
     */
    private fun initListAdapter() {
        binding.rvPictures.adapter = adapter

        // Submitting ne data to the adapter is done here instead of using a BindingAdapter
        // since lifecycle aware coroutine is required to submit the data
        viewModel.pictures.observe(this, {
            lifecycleScope.launch { adapter.submitData(it) }
        })

        // listen to the paging load state, to display errors and the loading state in the UI
        adapter.addLoadStateListener { loadState ->

            // Show message that no picture for the given query was found
            binding.tvNoPictures.isVisible =
                loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0

            // Show list only if data initial data retrieval (.source.refresh) was successful and contains results
            binding.rvPictures.isVisible =
                loadState.source.refresh is LoadState.NotLoading && adapter.itemCount != 0
            // Show progress bar  during the initial data retrieval
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading

            // Show retry Button if initial data retrieval was unsuccessful
            binding.btRetry.isVisible = loadState.source.refresh is LoadState.Error

            // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.refresh as? LoadState.Error

            // Show snackbar with the error message
            errorState?.let {
                Snackbar.make(
                    binding.root,
                    "${it.error.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Initiates the picture search, collapses the search view and adds the query to the search history
     */
    private fun searchPicture(query: String) {
        viewModel.searchPictures(query)
        searchView.clearFocus()
        searchMenuItem.collapseActionView()
        SearchRecentSuggestions(this, AUTHORITY, SearchHistoryProvider.MODE)
            .saveRecentQuery(query, null)
    }

}
