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
import com.swat_uzb.weatherapp.utils.getDrawable
import com.swat_uzb.weatherapp.utils.hasLocationPermission
import com.swat_uzb.weatherapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

/**
 * This MainFragment allows the user to view current,24 hourly, and forecast next 5-day
 * weather data in a selected location on the screen.
 */
class MainFragment : BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {

    @Inject
    lateinit var hourlyWeatherAdapter: HourlyWeatherAdapter

    @Inject
    lateinit var dailyWeatherAdapter: DailyWeatherAdapter

    @Inject
    lateinit var weatherViewModel: WeatherViewModel


    override fun onViewCreate() {
        sharedViewModel.showLoading()
        // set menu
        setupMenu()

        // check if the DB is empty or not
        checkDb()

        // subscribe Ui data
        subscribeToUiData()

        // handle the method of swiping to refresh
        binding.refreshLayout.setOnRefreshListener {
            sharedViewModel.showLoading()
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

    /**
     * Check if the Db is empty or not
     * if the DB is empty navigate to SearchFragment
     */
    private fun checkDb() {
        lifecycleScope.launch(Dispatchers.IO) {

            sharedViewModel.getLocationsList().onSuccess { currentUiList ->
                when {
                    currentUiList.isEmpty() -> {
                        withContext(Dispatchers.Main) {
                            findNavController().navigate(R.id.nav_search_location)
                            sharedViewModel.showLoading()
                        }
                    }
                    sharedViewModel.favouriteLocationId == 0L -> {
                        val id = currentUiList[0].id
                        sharedViewModel.setFavouriteLocationId(id)
                        weatherViewModel.loadCurrentData(id)
                    }
                    else -> {
                        weatherViewModel.loadCurrentData(sharedViewModel.favouriteLocationId)
                    }
                }
            }
        }
    }

    private fun onLaunchRefresh(boolean: Boolean, update: Boolean) {
        if (boolean && !update) {
            sharedViewModel.showLoading()
            binding.refreshLayout.isRefreshing = true
            weatherViewModel.isUpdate = true
            updateWeatherData()
            binding.refreshLayout.isRefreshing = false
        }
    }

    private fun updateWeatherData() {
        val id = sharedViewModel.favouriteLocationId
        val isGranted = when (sharedViewModel.getLocation()) {
            CURRENT_LOCATION_LOCATION -> requireContext().hasLocationPermission()
            else -> false
        }
        lifecycleScope.launch(Dispatchers.IO) {
            if (isGranted) {
                delay(1500)
                sharedViewModel.getLastLocation { location ->
                    weatherViewModel.updateDataUi(
                        id, location
                    )
                }
            } else {
                weatherViewModel.updateDataUi(id, null, false)
                sharedViewModel.setCurrentLocationAddedValue(false)
            }
        }
    }

    private fun subscribeToUiData() {

        lifecycleScope.launch {
            weatherViewModel.uiState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { mainUiState ->
                    with(binding) {
                        mainUiState.currentUi?.let { current ->
                            fragmentMainCurrentWeatherIconImageView
                                .setImageResource(current.icon_url.getDrawable())
                            fragmentMainCurrentWeatherTemp.text =
                                getString(R.string.degree_sign, current.temp)
                            fragmentMainCurrentWeatherFeelsLikeTemp.text =
                                getString(R.string.feels_like, current.feels_like)
                            fragmentMainCurrentWeatherUpdateTime.text =
                                current.local_time
                            fragmentMainCurrentWeatherMaxMinTemp.text =
                                current.location
                            fragmentMainSunAstronomySunriseTime.text = current.sunrise
                            fragmentMainSunAstronomySunsetTime.text = current.sunset
                            fragmentMainAstronomyUvIndex.text = bindUvIndex(current.uv)
                            fragmentMainAstronomyHumidityPercentage.text =
                                getString(R.string.percentage_of, current.humidity)
                            fragmentMainAstronomyWindPercentage.text = current.wind_speed
                            fragmentMainMoonAstronomyMoonriseTime.text = current.moonrise
                            fragmentMainMoonAstronomyMoonsetTime.text = current.moonset
                            sharedViewModel.setCurrent(current)
                        }
                        hourlyWeatherAdapter.submitList(mainUiState.listHourlyUi)
                        dailyWeatherAdapter.submitList(mainUiState.listDailyUi)
                        Timer().schedule(500L) {
                            sharedViewModel.hideLoading()
                        }
                    }
                }
        }
        lifecycleScope.launch {
            weatherViewModel.uiError
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { error ->
                    sharedViewModel.onError(error)
                }
        }

        onLaunchRefresh(weatherViewModel.onRefreshFlow(), weatherViewModel.isUpdate)

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
                this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
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
                        sharedViewModel.showLoading()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun bindUvIndex(index: Int): String {
        val resId = when (index) {
            in 0..2 -> R.string.uv_scale_low
            in 3..5 -> R.string.uv_scale_moderate
            6, 7 -> R.string.uv_scale_high
            in 8..10 -> R.string.uv_scale_very_high
            else -> R.string.uv_scale_extreme
        }
        return getString(resId)
    }
}



