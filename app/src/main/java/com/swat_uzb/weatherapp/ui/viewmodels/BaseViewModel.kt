package com.swat_uzb.weatherapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swat_uzb.weatherapp.utils.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    open fun launchViewModelScope(doWork: suspend () -> Unit): Job {
        return viewModelScope.launch(viewModelScope.coroutineContext + Dispatchers.IO) {
            doWork()
        }
    }

    @Inject
    lateinit var prefs: SharedPreferencesHelper
    private var _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()
    fun showLoading() = launchViewModelScope {
        _isLoading.update { true }}
    fun hideLoading() = launchViewModelScope {
        _isLoading.update { false }}
    private var _showShort = MutableSharedFlow<Int>()
    val showShort: SharedFlow<Int> = _showShort.asSharedFlow()

    fun showMessageExistMessage() = launchViewModelScope { _showShort.emit(2) }
    fun onError(t: Throwable) {
        launchViewModelScope {
            val int = when (t) {
                is UnknownHostException -> 0
                is IOException -> 1
                is HttpException -> 2
                else -> 3
            }
            _showShort.emit(int)
            hideLoading()
        }
    }
}