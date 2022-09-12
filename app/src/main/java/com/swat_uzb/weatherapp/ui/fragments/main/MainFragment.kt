package com.swat_uzb.weatherapp.ui.fragments.main

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.databinding.FragmentMainBinding
import com.swat_uzb.weatherapp.ui.fragments.BaseFragment
import com.swat_uzb.weatherapp.utils.Constants.CURRENT_LOCATION_LOCATION
import com.swat_uzb.weatherapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import javax.inject.Inject


class MainFragment : BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {

    @Inject
    lateinit var hourlyWeatherAdapter: HourlyWeatherAdapter

    @Inject
    lateinit var dailyWeatherAdapter: DailyWeatherAdapter

    @Inject
    lateinit var weatherViewModel: WeatherViewModel

    override fun onViewCreate() {

        // set menu
        setupMenu()

        checkDb()

        // subscribe Ui data
        subscribeToUiData()


        // swipe to refresh method
        binding.refreshLayout.setOnRefreshListener {
            sharedViewModel.showProgressBar()
            updateWeatherData()
            binding.refreshLayout.apply {
                isRefreshing = false
            }
        }

        // setup recyclerViews
        setUpRecyclerView()

        // Hide soft keyboard
        hideKeyboard(requireActivity())

    }

    private fun checkDb() {
        sharedViewModel.showProgressBar()
        lifecycleScope.launch(Dispatchers.IO) {
            sharedViewModel.hideProgressBar()
            sharedViewModel.getLocationsList().onSuccess {
                when {
                    it.isEmpty() -> {
                        sharedViewModel.hideProgressBar()
                        withContext(Dispatchers.Main) {
                            findNavController().navigate(R.id.nav_search_location)
                        }
                    }
                    sharedViewModel.favouriteLocationId == 0L -> {
                        val id = it[0].id
                        sharedViewModel.setFavouriteLocationId(id)
                        weatherViewModel.loadCurrentData(id)
                        sharedViewModel.hideProgressBar()
                    }
                    else -> {
                        weatherViewModel.loadCurrentData(sharedViewModel.favouriteLocationId)
                        sharedViewModel.hideProgressBar()
                    }
                }
            }
        }
    }

    private fun onLaunchRefresh(boolean: Boolean, update: Boolean) {
        if (boolean && !update) {
            binding.refreshLayout.isRefreshing = true
            sharedViewModel.isUpdate = !update
            updateWeatherData()
            binding.refreshLayout.isRefreshing = false
        }
    }

    private fun updateWeatherData() {
        val id = sharedViewModel.favouriteLocationId
        val isGranted =
            when (sharedViewModel.getLocation()) {
                CURRENT_LOCATION_LOCATION -> !sharedViewModel.isNotLocationGranted()
                else -> false
            }
        lifecycleScope.launch(Dispatchers.IO) {
            if (isGranted) {
                delay(1500)
                sharedViewModel.getLastLocation {
                    weatherViewModel.updateDataUi(
                        id,
                        it
                    )
                }
            } else {
                weatherViewModel.updateDataUi(id, null, isGranted)
                sharedViewModel.setCurrentLocationAddedValue(isGranted)
            }
            sharedViewModel.hideProgressBar()
        }
    }

    private fun subscribeToUiData() {

        // On Launch Refresh
        with(weatherViewModel) {
            sharedViewModel.getOnLaunchRefresh()
                .observe(viewLifecycleOwner) { onLaunchRefresh(it, sharedViewModel.isUpdate) }

            current.observe(viewLifecycleOwner) {
                binding.currentEntity = it
                sharedViewModel.setCurrent(it)
            }
            hourlyForecast.observe(viewLifecycleOwner) {
                hourlyWeatherAdapter.submitList(it.filter { hourlyUi ->
                    sharedViewModel.compareDate(hourlyUi.date)
                })
            }

            dailyForecast.observe(viewLifecycleOwner) { dailyWeatherAdapter.submitList(it) }
        }

        lifecycleScope.launch {
            weatherViewModel.error
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    sharedViewModel.handleError(it)
                }
        }
    }


    private fun setUpRecyclerView() {
        // setup daily Weather forecast recyclerview
        binding.fragmentMainDailyWeatherRecyclerView.apply {
            adapter = dailyWeatherAdapter
        }

        // setup hourly Weather forecast recyclerview
        binding.fragmentMainHourlyWeatherRecyclerView.apply {
            adapter = ScaleInAnimationAdapter(hourlyWeatherAdapter)
            OverScrollDecoratorHelper.setUpOverScroll(
                this,
                OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
            )
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_fragment_locations_menu, menu)
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
}



