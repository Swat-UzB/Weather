package com.swat_uzb.weatherapp.ui.fragments.search_location

import android.content.DialogInterface
import android.util.Log
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
import com.swat_uzb.weatherapp.utils.Constants.CURRENT_LOCATION_LOCATION
import com.swat_uzb.weatherapp.utils.Constants.NOT_SET
import com.swat_uzb.weatherapp.utils.CustomDialogFragment
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import javax.inject.Inject


class SearchLocationFragment :
    BaseFragment<FragmentSearchLocationBinding>(FragmentSearchLocationBinding::inflate),
    SearchView.OnQueryTextListener {

    @Inject
    lateinit var searchAdapter: SearchAdapter

    @Inject
    lateinit var searchViewModel: SearchViewModel

    override fun onViewCreate() {
        // Check permission isGranted
        checkLocation()

        // Observe Data
        subscribeData()

        // set menu
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

        // RecyclerView OnItemClick
        searchAdapter.setOnItemClickListener {
            // Adding new location
            searchViewModel.addLocation(it.lat, it.lon)
        }

        binding.addCurrentButton.setOnClickListener {
            sharedViewModel.clearLocationFromShare()
            sharedViewModel.setIsFirstTime(true)
            checkLocation()
        }

        // setup RecyclerView
        setUpRecyclerView()


    }

    private fun subscribeData() {
        sharedViewModel.hideProgressBar()
        lifecycleScope.launch {
            searchViewModel.showProgressBar
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { visible ->
                    with(binding.fragmentSearchProgressBar) {
                        visibility = if (visible) {
                            binding.lottieAnim.visibility = View.GONE
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
        }
        lifecycleScope.launch {
            searchViewModel.searchList
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    searchAdapter.submitList(it)
                    if (it.isEmpty()) {
                        checkAddButton()
                        binding.lottieAnim.visibility = View.VISIBLE
                    } else {
                        binding.lottieAnim.visibility = View.INVISIBLE
                        binding.addCurrentButton.visibility = View.GONE
                    }
                }
        }
        lifecycleScope.launch {
            sharedViewModel.addLocation
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    searchViewModel.addLocation(it.latitude, it.longitude, current = true)
                    sharedViewModel.setCurrentLocationAddedValue(true)

                }
        }
        lifecycleScope.launch {
            searchViewModel.isExistLocation
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    if (it) {
                        sharedViewModel.makeToast(getString(R.string.toast_exist))
                    } else {
                        findNavController().navigateUp()
                    }
                }
        }
        lifecycleScope.launch {
            searchViewModel.error
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    sharedViewModel.handleError(it)
                }
        }
    }

    private fun checkLocation() {
        checkAddButton()
        with(sharedViewModel) {
            when (getLocation()) {
                NOT_SET -> {
                    setupDialogFragment()
                    setupDialogFragmentListener()
                }
                CURRENT_LOCATION_LOCATION -> {

                    if (sharedViewModel.getIsFirstTime()) {
                        sharedViewModel.setIsFirstTime(false)
                        postCheckPermissionValue()
                    }
                }
            }
        }
    }

    private fun setupDialogFragment() {
        CustomDialogFragment.show(
            childFragmentManager,
            getString(R.string.use_current_location_title),
            getString(R.string.message_permission),
            getString(R.string.button_str_disagree),
            getString(R.string.button_str_agree)
        )
    }

    private fun setupDialogFragmentListener() {
        CustomDialogFragment.setupListener(childFragmentManager, viewLifecycleOwner) {
            with(sharedViewModel) {
                when (it) {
                    DialogInterface.BUTTON_NEGATIVE -> {
                        setLocation(1)
                    }
                    DialogInterface.BUTTON_POSITIVE -> {
                        setLocation(0)
                        checkLocation()
                    }
                }
            }
        }
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

    private fun checkAddButton() {

        binding.addCurrentButton.visibility =
            if (sharedViewModel.isCurrentLocationAdded()) View.GONE else View.VISIBLE
    }

    private fun getSearchList(query: String?) {
        searchViewModel.getSearchResultList(query)
    }
}