package com.calamity.weather.ui.mainactivity

import androidx.lifecycle.*
import com.calamity.weather.utils.Variables
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InternetViewModel @Inject constructor() : ViewModel() {

    private val observer = Observer<Boolean> {
        onInternetAccessChanged()
    }

    init {
        Variables.isNetworkConnectedLive.observeForever(observer)
    }

    private fun onInternetAccessChanged() = viewModelScope.launch {
        eventChannel?.send(Variables.isNetworkConnected)
    }

    private val eventChannel: Channel<Boolean>? = Channel<Boolean>()
    val event = eventChannel?.receiveAsFlow()

    override fun onCleared() {
        Variables.isNetworkConnectedLive.removeObserver(observer)
        super.onCleared()
    }
}
