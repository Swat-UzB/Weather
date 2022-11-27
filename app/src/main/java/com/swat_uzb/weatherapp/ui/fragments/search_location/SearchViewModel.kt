package com.swat_uzb.weatherapp.ui.fragments.search_location

import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.ForecastData
import com.swat_uzb.weatherapp.data.model.weatherapi.search.Search
import com.swat_uzb.weatherapp.domain.usecase.CheckLocationIsExistUseCase
import com.swat_uzb.weatherapp.domain.usecase.FetchForecastFromWeatherApi
import com.swat_uzb.weatherapp.domain.usecase.GetSearchResultListUseCase
import com.swat_uzb.weatherapp.domain.usecase.SaveNewLocationUseCase
import com.swat_uzb.weatherapp.ui.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

data class SearchUiState(
    val resultList: List<Search> = emptyList(),
    val isEmpty: Boolean = true,
    val isCurrentExist: Boolean = false,
    val isAddedLocation: Boolean = false
)

class SearchViewModel @Inject constructor(
    private val fetchForecastFromWeatherApi: FetchForecastFromWeatherApi,
    private val getSearchResultListUseCase: GetSearchResultListUseCase,
    private val saveNewLocationUseCase: SaveNewLocationUseCase,
    private val checkLocationIsExistUseCase: CheckLocationIsExistUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _uiError = MutableSharedFlow<Throwable>()
    val uiError: SharedFlow<Throwable> = _uiError.asSharedFlow()

    private val _uiAlreadyExist = MutableSharedFlow<Unit>()
    val uiAlreadyExist: SharedFlow<Unit> = _uiAlreadyExist.asSharedFlow()

    fun getSearchResultList(searchQuery: String?) {
        if (searchQuery.checkQuery()) {
            launchViewModelScope {
                getSearchResultListUseCase.getSearchResultList(searchQuery!!)
                    .onSuccess { searchList ->
                        _uiState.update { currentUiState ->
                            currentUiState.copy(
                                resultList = searchList,
                                isEmpty = searchList.isEmpty()
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiError.emit(error)
                        _uiState.update { currentUiState ->
                            currentUiState.copy(isEmpty = true, resultList = emptyList())
                        }
                    }
            }
        }
    }

    fun addLocation(lat: Double, lon: Double, id: Long = 0L, current: Boolean = false) {
        launchViewModelScope {
            checkLocationIsExistUseCase.checkLocationIsExist(lat, lon)
                .onSuccess { isExist ->
                    if (isExist && !current) {
                        _uiAlreadyExist.emit(Unit)
                    } else {
                        fetchForecastFromWeatherApi.fetchForecastFromWeatherApi("$lat,$lon")
                            .onSuccess { forecastData ->
                                addNewLocation(forecastData, id, current)
                            }
                            .onFailure { error ->
                                _uiError.emit(error)
                            }
                    }
                }
        }
    }

    private fun addNewLocation(
        forecastData: ForecastData,
        id: Long,
        current: Boolean
    ) {
        launchViewModelScope {
            supervisorScope {
                saveNewLocationUseCase.addNewLocation(forecastData, id, current)
                    .onSuccess {
                        _uiState.update { currentUiState ->
                            currentUiState.copy(isAddedLocation = true)
                        }
                    }
            }
        }
    }

}

fun String?.checkQuery() = (this != null) && (this.trim().length > 2)
