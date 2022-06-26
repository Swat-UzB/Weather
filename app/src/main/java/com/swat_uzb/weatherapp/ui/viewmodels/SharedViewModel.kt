package com.swat_uzb.weatherapp.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.domain.usecase.FetchForecastFromWeatherApi
import com.swat_uzb.weatherapp.domain.usecase.GetLocationsListAsFlowUseCase
import com.swat_uzb.weatherapp.domain.usecase.GetLocationsListUseCase
import com.swat_uzb.weatherapp.domain.usecase.SaveNewLocationUseCase
import com.swat_uzb.weatherapp.utils.Constants.DEFAULT_LOCATION
import com.swat_uzb.weatherapp.utils.Constants.TIME_STRING_FORMAT
import com.swat_uzb.weatherapp.utils.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedViewModel @Inject constructor(
    private val context: Application,
    private val fetchForecastFromWeatherApi: FetchForecastFromWeatherApi,
    private val saveNewLocationUseCase: SaveNewLocationUseCase,
    private val getLocationsListUseCase: GetLocationsListUseCase,
    private val fusedLocationClient: FusedLocationProviderClient,
    getLocationsListAsFlowUseCase: GetLocationsListAsFlowUseCase,
    private val prefs: SharedPreferencesHelper
) : ViewModel() {

    var isUpdate = false

    val allLocations = getLocationsListAsFlowUseCase.getLocationsList

    private val _current = MutableLiveData<CurrentUi>()
    val current: LiveData<CurrentUi> get() = _current

    private var _showProgressBar = MutableStateFlow(false)
    val showProgressBar: StateFlow<Boolean> = _showProgressBar.asStateFlow()

    private var _favouriteLocationId = 0L
    val favouriteLocationId get() = _favouriteLocationId

    private var _checkPermission = MutableSharedFlow<Unit>()
    val checkPermission: SharedFlow<Unit> = _checkPermission.asSharedFlow()

    private var _navigateUp = MutableSharedFlow<Unit>()
    val navigateUp: SharedFlow<Unit> = _navigateUp.asSharedFlow()


    suspend fun getLocationsList() = getLocationsListUseCase.getLocationsList()

    fun addCurrentLocation() {
        showProgressBar()
        getLastLocation {
            viewModelScope.launch(Dispatchers.IO) {
                fetchForecastFromWeatherApi.fetchForecastFromWeatherApi("${it.latitude},${it.longitude}")
                    .onSuccess { forecastData ->
                        saveNewLocationUseCase.addNewLocation(forecastData, 1, true)
                            .onSuccess {
                                prefs.setCurrent(true)
                                hideProgressBar()
                                _navigateUp.emit(Unit)
                            }
                    }
                    .onFailure {
                        handleError(it)
                        hideProgressBar()
                    }
            }
        }
    }


    fun getCurrentLocation() = prefs.getCurrent()

    fun enableCurrentLocation() = prefs.setCurrent(false)

    fun showProgressBar() {
        viewModelScope.launch {
            _showProgressBar.emit(true)
        }
    }

    fun setCurrent(currentUi: CurrentUi) {
        _current.postValue(currentUi)
    }

    fun hideProgressBar() {
        viewModelScope.launch {
            delay(500)
            _showProgressBar
                .emit(false)
        }
    }

    fun setFavouriteLocationId(id: Long) {
        _favouriteLocationId = id
    }

    private var _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location>
        get() = _currentLocation

    fun setLocation(location: Location) {
        _currentLocation.postValue(location)
    }

    fun notSetLocation() = getLocation() == DEFAULT_LOCATION

    fun getOnLaunchRefresh(): LiveData<Boolean> = prefs.getRefreshOnLaunch()

    fun getAutoRefresh(): String = prefs.getAutoRefresh()!!

    fun getNotification() = prefs.getNotification()

    fun getLocation(): String = prefs.getLocation()!!

    fun setLocation(int: Int) = prefs.setLocation(int)

    fun postCheckPermissionValue() {
        viewModelScope.launch(Dispatchers.IO) {
            _checkPermission.emit(Unit)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun compareDate(string: String): Boolean {
        val simpleDate = SimpleDateFormat(TIME_STRING_FORMAT)
        val hourlyDate = simpleDate.parse(string)
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 1)
        val tomorrow = calendar.time
        return today.before(hourlyDate) && tomorrow.after(hourlyDate)
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(onLocation: (location: Location) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    onLocation(it)
                }
            }
    }

    fun makeToast(text: String) {
        Toast.makeText(
            context,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun handleError(e: Throwable) {
        val errorMessage = when (e) {
            is IOException -> context.getString(R.string.no_network_str)
            is HttpException -> context.getString(R.string.something_wrong_str)
            else -> e.localizedMessage
        }
        makeToast(errorMessage)
    }
}