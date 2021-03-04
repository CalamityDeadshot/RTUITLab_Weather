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
    private val internetAccess: LiveData<Boolean> = Variables.isNetworkConnectedLive

    init {
        this.internetAccess.observeForever {
            onInternetAccessChanged()
        }
    }

    private fun onInternetAccessChanged() = viewModelScope.launch {
        eventChannel.send(internetAccess.value!!)
    }

    private val eventChannel = Channel<Boolean>()
    val event = eventChannel.receiveAsFlow()
}
