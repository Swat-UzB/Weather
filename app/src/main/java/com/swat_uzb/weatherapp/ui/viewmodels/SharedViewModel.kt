package com.swat_uzb.weatherapp.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.domain.usecase.GetLocationsListAsFlowUseCase
import com.swat_uzb.weatherapp.domain.usecase.GetLocationsListUseCase
import com.swat_uzb.weatherapp.utils.ConnectivityObserver
import com.swat_uzb.weatherapp.utils.hasLocationPermission
import com.swat_uzb.weatherapp.utils.isGpsOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedViewModel @Inject constructor(
    private val application: Application,
    private val locationManager: LocationManager,
    private val getLocationsListUseCase: GetLocationsListUseCase,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val connectivityObserver: ConnectivityObserver,
    getLocationsListAsFlowUseCase: GetLocationsListAsFlowUseCase,
) : BaseViewModel() {

    private var networkStatus = ConnectivityObserver.Status.Unavailable
    val allLocations = getLocationsListAsFlowUseCase.getLocationsList

    private val _current = MutableLiveData<CurrentUi?>()
    val current: LiveData<CurrentUi?> get() = _current

    private var _favouriteLocationId = 0L
    val favouriteLocationId get() = _favouriteLocationId


    private val _addLocation = MutableSharedFlow<Location>()
    val addLocation = _addLocation.asSharedFlow()

    private var _checkPermission = MutableSharedFlow<Unit>()
    val checkPermission: SharedFlow<Unit> = _checkPermission.asSharedFlow()

    init {
        launchViewModelScope {
            connectivityObserver.observe().collect {
                networkStatus = it
            }
        }

    }

    suspend fun getLocationsList() = getLocationsListUseCase.getLocationsList()

    fun addCurrentLocation() {
        getLastLocation { location ->
            viewModelScope.launch(Dispatchers.IO) {
                _addLocation.emit(location)
                setCurrentLocationAddedValue(true)
            }
        }
    }

    fun setCurrent(currentUi: CurrentUi?) {
        _current.postValue(currentUi)
    }

    fun setFavouriteLocationId(id: Long) {
        _favouriteLocationId = id
    }

    fun getAutoRefresh(): String = prefs.getAutoRefresh()!!

    fun getNotification() = prefs.getNotification()

    fun getLocation(): String = prefs.getLocation()!!

    fun setLocation(int: Int) = prefs.setLocation(int)

    fun postCheckPermissionValue() {
        viewModelScope.launch(Dispatchers.IO) {
            _checkPermission.emit(Unit)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(onLocation: (location: Location) -> Unit) {

        when {
            (networkStatus != ConnectivityObserver.Status.Available) -> {
                onError(UnknownHostException())
            }
            (application.hasLocationPermission() && locationManager.isGpsOn()) -> {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener {
                        it?.let(onLocation)
                    }
            }
            else -> {
                postCheckPermissionValue()

            }
        }
    }


    fun isCurrentLocationAdded() = prefs.getCurrent()

    fun setCurrentLocationAddedValue(boolean: Boolean) = prefs.setCurrent(boolean)

    fun clearLocationFromShare() = prefs.clearLocationData()

    fun getIsFirstTime() = prefs.getIsFirstTime()

    fun setIsFirstTime(boolean: Boolean) = prefs.setIsFistTime(boolean)

}