package com.swat_uzb.weatherapp.ui.fragments.add_location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swat_uzb.weatherapp.domain.usecase.DeleteLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddLocationViewModel @Inject constructor(
    private val deleteLocationUseCase: DeleteLocationUseCase,
) : ViewModel() {

    fun deleteCurrent(currentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteLocationUseCase.deleteLocation(currentId)
        }
    }
}