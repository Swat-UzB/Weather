package com.swat_uzb.weatherapp.ui.fragments.add_location

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.viewModels
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import javax.inject.Inject


class AddLocationFragment :
    BaseFragment<FragmentAddLocationBinding>(FragmentAddLocationBinding::inflate) {

    @Inject
    lateinit var locationsAdapter: LocationsAdapter

    private val addLocationViewModel: AddLocationViewModel by viewModels { viewModelFactory }

    override fun onViewCreate() {

        sharedViewModel.showProgressBar()

        checkDb()

        // observe LiveData CurrentWeather
        subscribeData()

        // set menu
        setHasOptionsMenu(true)

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
                when {
                    // when item id equal favouriteLocationId
                    itemToDelete.id == sharedViewModel.favouriteLocationId -> {
                        // set favouriteLocationId 0
                        sharedViewModel.setFavouriteLocationId(0L)
                    }
                    // when item current location
                    itemToDelete.current_location -> {
                        // set isCurrentId value false

                        sharedViewModel.enableCurrentLocation()
                    }
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
                    .collect {
                        locationsAdapter.submitList(it)
                        hideProgressBar()
                    }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_location_fragment_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_add_location) {
            navigateToSearchFragment()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToSearchFragment() {
        // navigate to SearchLocationFragment
        findNavController().navigate(R.id.action_addLocationFragment_to_searchLocationFragment)
    }

    private fun checkDb() {

        // getting current locations list from Db
        lifecycleScope.launch(Dispatchers.IO) {
            sharedViewModel.getLocationsList().onSuccess {
                if (it.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        navigateToSearchFragment()
                    }
                }
            }
        }
    }
}