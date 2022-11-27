package com.swat_uzb.weatherapp.ui.fragments.main

import android.location.Location
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.domain.model.DailyUi
import com.swat_uzb.weatherapp.domain.model.HourlyUi
import com.swat_uzb.weatherapp.domain.model.compareDate
import com.swat_uzb.weatherapp.domain.usecase.*
import com.swat_uzb.weatherapp.ui.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

data class MainUiState(
    val currentUi: CurrentUi? = null,
    val listDailyUi: List<DailyUi> = emptyList(),
    val listHourlyUi: List<HourlyUi> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

@Singleton
class WeatherViewModel @Inject constructor(
    private val loadLocationDataUseCase: LoadLocationDataUseCase,
    private val loadDailyByCurrentIdUseCase: LoadDailyByCurrentIdUseCase,
    private val loadHourlyListByCurrentId: LoadHourlyListByCurrentIdUseCase,
    private val getLocationsListUseCase: GetLocationsListUseCase,
    private val fetchForecastFromWeatherApi: FetchForecastFromWeatherApi,
    private val updateLocationDataUseCase: UpdateLocationDataUseCase
) : BaseViewModel() {

    var isUpdate = false
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _uiError = MutableSharedFlow<Throwable>()
    val uiError: SharedFlow<Throwable> = _uiError.asSharedFlow()


    fun onRefreshFlow() = prefs.isRefreshOnLaunchOn()

    fun loadCurrentData(favouriteLocationId: Long) {
        launchViewModelScope {
            val current = loadLocationDataUseCase.loadLocationData(favouriteLocationId)
            val hourly = loadHourlyListByCurrentId.getHourlyList(favouriteLocationId)
            val daily = loadDailyByCurrentIdUseCase.getDailyList(favouriteLocationId)
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    currentUi = current,
                    listHourlyUi = hourly.filter { it.compareDate() },
                    listDailyUi = daily
                )
            }
        }
    }

    fun updateDataUi(id: Long, location: Location?, isGranted: Boolean = true) {
        launchViewModelScope {
            getLocationsListUseCase.getLocationsList().onSuccess { listCurrentUi ->
                listCurrentUi.map { currentUi ->
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
                        .onFailure { error ->
                            hideLoading()
                            _uiError.emit(error)
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

