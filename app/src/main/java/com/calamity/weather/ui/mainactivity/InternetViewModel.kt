package com.calamity.weather.ui.mainactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.calamity.weather.utils.Variables
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InternetViewModel @Inject constructor() : ViewModel() {
    val internetAccess: LiveData<Boolean> = Variables.isNetworkConnectedLive
}