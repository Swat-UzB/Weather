package com.swat_uzb.weatherapp.ui.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.domain.usecase.GetLocationsListAsFlowUseCase
import com.swat_uzb.weatherapp.domain.usecase.GetLocationsListUseCase
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
    private var locationManager: LocationManager,
    private val context: Application,
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

    private val _addLocation = MutableSharedFlow<Location>()
    val addLocation = _addLocation.asSharedFlow()

    private var _checkPermission = MutableSharedFlow<Unit>()
    val checkPermission: SharedFlow<Unit> = _checkPermission.asSharedFlow()

    suspend fun getLocationsList() = getLocationsListUseCase.getLocationsList()

    fun isNotLocationGranted() =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

    private fun isGpsOn() =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    fun addCurrentLocation() {
        showProgressBar()
        getLastLocation {

            it.let {
                viewModelScope.launch(Dispatchers.IO) {
                    _addLocation.emit(it)
                }
            }
        }
    }

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

    fun getAutoRefresh(): String = prefs.getAutoRefresh()!!

    fun getNotification() = prefs.getNotification()

    fun getOnLaunchRefresh(): LiveData<Boolean> = prefs.isRefreshOnLaunchOn()

    fun getLocation(): String = prefs.getLocation()!!

    fun setLocation(int: Int) = prefs.setLocation(int)

    fun postCheckPermissionValue() {
        viewModelScope.launch(Dispatchers.IO) {
            _checkPermission.emit(Unit)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(onLocation: (location: Location) -> Unit) {
        if (isNotLocationGranted() || !isGpsOn()) {
            postCheckPermissionValue()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                viewModelScope.launch {
                    it.let(onLocation)
                }
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

    fun isCurrentLocationAdded() = prefs.getCurrent()

    fun setCurrentLocationAddedValue(boolean: Boolean) = prefs.setCurrent(boolean)

    fun clearLocationFromShare() = prefs.clearLocationData()

    fun getIsFirstTime() = prefs.getIsFirstTime()

    fun setIsFirstTime(boolean: Boolean) = prefs.setIsFistTime(boolean)

}