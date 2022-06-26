package com.swat_uzb.weatherapp.ui.fragments.search_location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.ForecastData
import com.swat_uzb.weatherapp.data.model.weatherapi.search.Search
import com.swat_uzb.weatherapp.domain.usecase.CheckLocationIsExistUseCase
import com.swat_uzb.weatherapp.domain.usecase.FetchForecastFromWeatherApi
import com.swat_uzb.weatherapp.domain.usecase.GetSearchResultListUseCase
import com.swat_uzb.weatherapp.domain.usecase.SaveNewLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val fetchForecastFromWeatherApi: FetchForecastFromWeatherApi,
    private val getSearchResultListUseCase: GetSearchResultListUseCase,
    private val saveNewLocationUseCase: SaveNewLocationUseCase,
    private val checkLocationIsExistUseCase: CheckLocationIsExistUseCase
) : ViewModel() {

    private var _showProgressBar = MutableStateFlow(false)
    val showProgressBar = _showProgressBar.asStateFlow()

    private var _error = MutableSharedFlow<Throwable>()
    val error: SharedFlow<Throwable> = _error.asSharedFlow()

    private suspend fun showProgressBar() {
        _showProgressBar.emit(true)
    }

    private suspend fun hideProgressBar() {
        _showProgressBar.emit(false)
    }

    private val _searchList =
        MutableSharedFlow<List<Search>>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val searchList: SharedFlow<List<Search>> = _searchList.asSharedFlow()

    private val _isExistLocation =
        MutableSharedFlow<Boolean>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val isExistLocation: SharedFlow<Boolean> = _isExistLocation.asSharedFlow()

    fun getSearchResultList(searchQuery: String?) {
        if (searchQuery.checkQuery()) {
            viewModelScope.launch(Dispatchers.IO) {
                showProgressBar()
                getSearchResultListUseCase.getSearchResultList(searchQuery!!)
                    .onSuccess {
                        hideProgressBar()
                        _searchList.emit(it)
                    }
                    .onFailure {
                        hideProgressBar()
                        _searchList.emit(emptyList())
                        _error.emit(it)
                    }
            }
        }
    }

    fun addLocation(search: Search) {
        viewModelScope.launch(Dispatchers.IO) {
            showProgressBar()
            checkLocationIsExistUseCase.checkLocationIsExist(search.lat, search.lon)
                .onSuccess { isExist ->
                    hideProgressBar()
                    if (isExist) {
                        _isExistLocation.emit(true)
                    } else {
                        fetchForecastFromWeatherApi.fetchForecastFromWeatherApi((search.getLocation()))
                            .onSuccess {
                                addNewLocation(it)
                            }
                            .onFailure { _error.emit(it) }
                    }
                }
        }
    }

    private fun addNewLocation(forecastData: ForecastData) {
        viewModelScope.launch(Dispatchers.IO) {
            saveNewLocationUseCase.addNewLocation(forecastData)
                .onSuccess {
                    _isExistLocation.emit(false)
                }
        }
    }


}

fun String?.checkQuery() = this != null && this.trim().length > 2

fun Search.getLocation() = "${lat},${lon}"
