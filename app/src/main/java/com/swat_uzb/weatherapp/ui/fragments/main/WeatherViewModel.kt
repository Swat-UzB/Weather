package com.swat_uzb.weatherapp.ui.fragments.main

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.domain.model.DailyUi
import com.swat_uzb.weatherapp.domain.model.HourlyUi
import com.swat_uzb.weatherapp.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherViewModel @Inject constructor(
    private val loadLocationDataUseCase: LoadLocationDataUseCase,
    private val loadDailyByCurrentIdUseCase: LoadDailyByCurrentIdUseCase,
    private val loadHourlyListByCurrentId: LoadHourlyListByCurrentIdUseCase,
    private val getLocationsListUseCase: GetLocationsListUseCase,
    private val fetchForecastFromWeatherApi: FetchForecastFromWeatherApi,
    private val updateLocationDataUseCase: UpdateLocationDataUseCase
) : ViewModel() {
    private val _current = MutableLiveData<CurrentUi>()
    val current: LiveData<CurrentUi> get() = _current

    private val _dailyForecast = MutableLiveData<List<DailyUi>>()
    val dailyForecast: LiveData<List<DailyUi>> get() = _dailyForecast

    private val _hourlyForecast = MutableLiveData<List<HourlyUi>>()
    val hourlyForecast: LiveData<List<HourlyUi>> get() = _hourlyForecast

    private var _error = MutableSharedFlow<Throwable>()
    val error: SharedFlow<Throwable> = _error.asSharedFlow()

    fun loadCurrentData(favouriteLocationId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = loadLocationDataUseCase.loadLocationData(favouriteLocationId)
            _current.postValue(current)
            val hourly = loadHourlyListByCurrentId.getHourlyList(favouriteLocationId)
            _hourlyForecast.postValue(hourly)
            val daily = loadDailyByCurrentIdUseCase.getDailyList(favouriteLocationId)
            _dailyForecast.postValue(daily)
        }
    }

    fun updateDataUi(id: Long, location: Location?, isGranted: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            getLocationsListUseCase.getLocationsList().onSuccess { it ->
                it.map { currentUi ->
                    var currentUiNew = currentUi
                    when {
                        currentUi.current_location && !isGranted -> {
                            currentUiNew = currentUi.copy(current_location = false)
                        }

                        (currentUi.current_location && location != null && isGranted) -> {
                            currentUiNew = currentUi.copy(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        }
                    }
                    fetchForecastFromWeatherApi.fetchForecastFromWeatherApi(currentUiNew.locationToString())
                        .onSuccess { data ->
                            updateLocationDataUseCase.updateLocationData(
                                currentUiNew, data
                            )
                                .onSuccess {
                                    when (currentUi.id) {
                                        id -> loadCurrentData(id)
                                    }
                                }
                        }
                        .onFailure {
                            _error.emit(it)
                            return@launch
                        }
                }
            }
        }
    }
}

fun Any.locationToString(): String = when (this) {
    is Location -> {
        "${this.latitude},${this.longitude} "
    }
    is CurrentUi -> {
        "${this.latitude}, ${this.longitude}"
    }
    else -> ""
}

