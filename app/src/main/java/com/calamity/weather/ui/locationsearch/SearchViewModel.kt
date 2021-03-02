package com.calamity.weather.ui.locationsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.data.repository.AutocompleteRepository
import com.calamity.weather.data.repository.WeatherRepository
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AutocompleteRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    fun provideClient(client: PlacesClient) {
        repository.provideClient(client)
    }

    val searchQuery = MutableStateFlow("")

    private val autocompleteFlow = searchQuery.flatMapLatest {
        repository.getPredictions(it)
    }

    val predictions = autocompleteFlow.asLiveData()

    suspend fun onAddPlace(place: PlacesPrediction, added: Boolean) {
        viewModelScope.launch {
            repository.update(place, added)
            if (added) weatherRepository.addWeather(place)
            else weatherRepository.delete(place)
            sendEvent(searchQuery.value)
        }
    }

    fun onDestroy() = viewModelScope.launch {repository.clearDb()}

    private suspend fun sendEvent(query: String) {
        eventChannel.send(UpdateEvent.UpdateList(repository.getPredictionsAsList(query)))
    }

    private val eventChannel = Channel<UpdateEvent>()
    val event = eventChannel.receiveAsFlow()

    sealed class UpdateEvent {
        data class UpdateList(val list: List<PlacesPrediction>) : UpdateEvent()
    }
}