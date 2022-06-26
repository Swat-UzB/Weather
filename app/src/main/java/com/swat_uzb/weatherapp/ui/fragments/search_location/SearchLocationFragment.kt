package com.swat_uzb.weatherapp.ui.fragments.search_location

import android.content.DialogInterface
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.databinding.FragmentSearchLocationBinding
import com.swat_uzb.weatherapp.ui.fragments.BaseFragment
import com.swat_uzb.weatherapp.utils.Constants.CURRENT_LOCATION_LOCATION
import com.swat_uzb.weatherapp.utils.Constants.DEFAULT_LOCATION
import com.swat_uzb.weatherapp.utils.CustomDialogFragment
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import javax.inject.Inject


class SearchLocationFragment :
    BaseFragment<FragmentSearchLocationBinding>(FragmentSearchLocationBinding::inflate),
    SearchView.OnQueryTextListener {

    @Inject
    lateinit var searchAdapter: SearchAdapter

    private val searchViewModel: SearchViewModel by viewModels { viewModelFactory }

    override fun onViewCreate() {

        checkPermission()

        // set menu
        setHasOptionsMenu(true)

        searchAdapter.setOnItemClickListener {
            // Adding new location
            searchViewModel.addLocation(it)
        }

        // setup RecyclerView
        setUpRecyclerView()

        // Observe Data
        subscribeData()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_location_menu, menu)
        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    private fun subscribeData() {
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
                        binding.lottieAnim.visibility = View.VISIBLE
                    } else {
                        binding.lottieAnim.visibility = View.INVISIBLE
                    }
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
        lifecycleScope.launch {
            sharedViewModel.navigateUp
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    findNavController().navigateUp()
                }
        }
    }

    private fun getSearchList(query: String?) {
        searchViewModel.getSearchResultList(query)
    }

    private fun setupDialogFragment() {
        CustomDialogFragment.show(
            childFragmentManager,
            getString(R.string.use_current_location_title),
            getString(R.string.permission_required),
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
                        checkPermission()
                    }
                }
            }
        }
    }

    private fun checkPermission() {
        Log.d("TTTT", sharedViewModel.getLocation())
        when (sharedViewModel.getLocation()) {
            DEFAULT_LOCATION -> {
                setupDialogFragment()
                setupDialogFragmentListener()
            }
            CURRENT_LOCATION_LOCATION -> {
                if (!sharedViewModel.getCurrentLocation()) {
                    sharedViewModel.postCheckPermissionValue()
                }
            }
        }
    }
}