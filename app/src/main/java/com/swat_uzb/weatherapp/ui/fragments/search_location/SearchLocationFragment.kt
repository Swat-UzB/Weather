package com.swat_uzb.weatherapp.ui.fragments.search_location

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.databinding.FragmentSearchLocationBinding
import com.swat_uzb.weatherapp.ui.fragments.BaseFragment
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import javax.inject.Inject

/**
 * This fragment allows the user to search locations,
 * view list of locations on the screen and locations to db  .
 */
class SearchLocationFragment : BaseFragment<FragmentSearchLocationBinding>
    (FragmentSearchLocationBinding::inflate),
    SearchView.OnQueryTextListener {

    @Inject
    lateinit var searchAdapter: SearchAdapter

    @Inject
    lateinit var searchViewModel: SearchViewModel

    override fun onViewCreate() {
        // Check permission isGranted
        checkAddButton()
        // Observe Data
        subscribeData()

        // set menu
        setUpMenu()

        // RecyclerView OnItemClick
        searchAdapter.setOnItemClickListener { search ->
            // Adding new location
            sharedViewModel.showLoading()
            searchViewModel.addLocation(search.lat, search.lon)
        }

        // on click the addButton add current location to db
        binding.addCurrentButton.setOnClickListener {
            with(sharedViewModel) {
                showLoading()
                clearLocationFromShare()
                setIsFirstTime(true)
                setLocation(0)
            }
            checkLocation()
        }

        // setup RecyclerView
        setUpRecyclerView()
    }


    private fun setUpMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_location_menu, menu)
                val search = menu.findItem(R.id.menu_search)
                val searchView = search.actionView as? SearchView
                searchView?.isSubmitButtonEnabled = true
                searchView?.setOnQueryTextListener(this@SearchLocationFragment)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_add_location -> {
                        findNavController().navigate(R.id.action_mainFragment_to_addLocationFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun subscribeData() {
        val lottieVis = binding.lottieAnim
        lifecycleScope.launch {
            searchViewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { searchUiState ->
                    searchAdapter.submitList(searchUiState.resultList)

                    if (searchUiState.isEmpty) {
                        checkAddButton()
                        lottieVis.visibility = View.VISIBLE
                    } else {
                        lottieVis.visibility = View.INVISIBLE
                        binding.addCurrentButton.visibility = View.GONE
                    }
                    if (searchUiState.isAddedLocation) {
                        findNavController().navigateUp()
                    }
                    sharedViewModel.hideLoading()
                }
        }
        lifecycleScope.launch {
            sharedViewModel.addLocation
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { location ->
                    searchViewModel.addLocation(
                        location.latitude,
                        location.longitude,
                        current = true
                    )
                }
        }
        lifecycleScope.launch {
            searchViewModel.uiError
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { error ->
                    sharedViewModel.onError(error)
                }
        }
        lifecycleScope.launch {
            searchViewModel.uiAlreadyExist
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    sharedViewModel.showMessageExistMessage()
                }
        }
    }

    private fun checkLocation() {
        checkAddButton()
        with(sharedViewModel) {
            if (getIsFirstTime()) {
                setIsFirstTime(false)
                postCheckPermissionValue()
            }
        }
    }

    private fun checkAddButton() {
        binding.addCurrentButton.visibility =
            if (sharedViewModel.isCurrentLocationAdded()) View.GONE else View.VISIBLE
    }

    private fun setUpRecyclerView() = binding.fragmentSearchLocationSearchRecyclerView.apply {
        adapter = searchAdapter
        OverScrollDecoratorHelper.setUpOverScroll(
            this,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        getSearchList(query)
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        getSearchList(query)
        return true
    }

    private fun getSearchList(query: String?) {
        searchViewModel.getSearchResultList(query)
    }
}