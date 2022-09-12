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

class AddLocationFragment :
    BaseFragment<FragmentAddLocationBinding>(FragmentAddLocationBinding::inflate) {

    @Inject
    lateinit var locationsAdapter: LocationsAdapter

    @Inject
    lateinit var addLocationViewModel: AddLocationViewModel

    override fun onViewCreate() {

        // observe LiveData CurrentWeather
        subscribeData()

        // set menu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_location_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_add_location -> {
                        // navigate to SearchLocationFragment
                        findNavController().navigate(R.id.action_addLocationFragment_to_searchLocationFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // setup recyclerView
        setUpRecyclerView()

        locationsAdapter.setOnItemClickListener {
            sharedViewModel.setFavouriteLocationId(it.id)

            // navigate to Main fragment
            findNavController().navigateUp()
        }

        // Hide soft keyboard
        hideKeyboard(requireActivity())
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

                // Delete Item
                addLocationViewModel.deleteCurrent(itemToDelete.id)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun subscribeData() {
        with(sharedViewModel) {
            lifecycleScope.launch {
                allLocations.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                    .collect { list ->
                        hideProgressBar()
                        if (list.isEmpty()) {
                            sharedViewModel.clearLocationFromShare()
                            sharedViewModel.setCurrentLocationAddedValue(false)
                            sharedViewModel.setIsFirstTime(true)
                            findNavController().navigate(R.id.nav_search_location)
                        }
                        locationsAdapter.submitList(list.sortedByDescending  { it.current_location })
                    }
            }
        }
    }
}