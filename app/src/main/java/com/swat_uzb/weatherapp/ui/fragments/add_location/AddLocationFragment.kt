package com.swat_uzb.weatherapp.ui.fragments.add_location

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.databinding.FragmentAddLocationBinding
import com.swat_uzb.weatherapp.ui.fragments.BaseFragment
import com.swat_uzb.weatherapp.utils.SwipeToDelete
import com.swat_uzb.weatherapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import javax.inject.Inject

/**
 * This fragment allows the user to remove a location
 * and view list of location on the screen.
 */
class AddLocationFragment :
    BaseFragment<FragmentAddLocationBinding>(FragmentAddLocationBinding::inflate) {

    @Inject
    lateinit var locationsAdapter: LocationsAdapter

    @Inject
    lateinit var addLocationViewModel: AddLocationViewModel

    override fun onViewCreate() {

        lifecycleScope.launch() {
            sharedViewModel.getLocationsList().onSuccess {
                if (it.isEmpty()) {
                    findNavController().navigate(R.id.nav_search_location)
                    sharedViewModel.showLoading()
                }
            }
        }

        // observe LiveData CurrentWeather
        subscribeData()

        // set menu
        setUpMenu()

        // setup recyclerView
        setUpRecyclerView()

        locationsAdapter.setOnItemClickListener { currentUi ->
            sharedViewModel.setFavouriteLocationId(currentUi.id)

            // navigate to Main fragment
            findNavController().navigateUp()
            sharedViewModel.showLoading()
        }

        // Hide soft keyboard
        hideKeyboard(requireActivity())
    }

    private fun setUpMenu() {
        // adding location menu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_location_fragment_menu, menu)
            }

            // navigate to search fragment when location menu pressed
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {

                    R.id.menu_add_location -> {
                        // navigate to SearchLocationFragment
                        findNavController().navigate(R.id.action_addLocationFragment_to_searchLocationFragment)
                        sharedViewModel.showLoading()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setUpRecyclerView() = binding.addLocationsLocationsRecyclerView.apply {
        adapter = ScaleInAnimationAdapter(locationsAdapter)
        OverScrollDecoratorHelper.setUpOverScroll(
            this,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )
        // swipe to delete
        swipeToDelete(this)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallBack = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val itemToDelete = locationsAdapter.currentList[viewHolder.adapterPosition]

                // when item id equal favouriteLocationId
                if (itemToDelete.id == sharedViewModel.favouriteLocationId) {
                    // set favouriteLocationId 0
                    sharedViewModel.setFavouriteLocationId(0L)
                }
                if (itemToDelete.current_location) {
                    sharedViewModel.setCurrentLocationAddedValue(false)
                }

                // if list of locations is empty navigates to searchLocationFragment

                with(sharedViewModel) {
                    if (locationsAdapter.currentList.size == 1) {
                        clearLocationFromShare()
                        setCurrentLocationAddedValue(false)
                        setIsFirstTime(true)
                        findNavController().navigate(R.id.nav_search_location)
                        sharedViewModel.showLoading()
                    }
                }
                // Delete Item
                addLocationViewModel.deleteCurrent(itemToDelete.id)

            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    /**
     * Subscribe to uiData
     */
    private fun subscribeData() {
        lifecycleScope.launch {
            addLocationViewModel.uiState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { currentUiState ->
                    locationsAdapter
                        .submitList(currentUiState.listCurrent.sortedByDescending { currentUi ->
                            currentUi.current_location
                        })
                    sharedViewModel.hideLoading()
                }
        }
    }
}