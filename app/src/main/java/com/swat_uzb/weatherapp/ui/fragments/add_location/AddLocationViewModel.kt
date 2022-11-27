package com.swat_uzb.weatherapp.ui.fragments.add_location

import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.domain.usecase.DeleteLocationUseCase
import com.swat_uzb.weatherapp.domain.usecase.GetLocationsListAsFlowUseCase
import com.swat_uzb.weatherapp.ui.viewmodels.BaseViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AddLocationUiState(
    val listCurrent: List<CurrentUi> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

class AddLocationViewModel @Inject constructor(
    private val deleteLocationUseCase: DeleteLocationUseCase,
    getLocationsListAsFlowUseCase: GetLocationsListAsFlowUseCase,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AddLocationUiState())
    val uiState: StateFlow<AddLocationUiState> = _uiState.asStateFlow()

    fun deleteCurrent(currentId: Long) {
        launchViewModelScope {
            coroutineScope {
                deleteLocationUseCase.deleteLocation(currentId)
            }
        }
    }

    init {
        launchViewModelScope {
            getLocationsListAsFlowUseCase.getLocationsList.collect { listCurrentData ->
                _uiState.update {
                    AddLocationUiState().copy(
                        listCurrent = listCurrentData,
                        isLoading = listCurrentData.isEmpty()
                    )
                }
            }
        }
    }
}