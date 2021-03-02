package com.calamity.weather.ui.locationsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.calamity.weather.data.repository.AutocompleteRepository
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AutocompleteRepository
) : ViewModel() {

    fun provideClient(client: PlacesClient) {
        repository.provideClient(client)
    }

    val searchQuery = MutableStateFlow("")

    private val autocompleteFlow = searchQuery.flatMapLatest {
        repository.getPredictions(it)
    }

    val predictions = autocompleteFlow.asLiveData()

    fun onDestroy() = viewModelScope.launch {repository.clearDb()}

}