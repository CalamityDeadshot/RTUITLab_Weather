package com.calamity.weather.ui.locationsearch

import androidx.lifecycle.*
import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.data.repository.AutocompleteRepository
import com.calamity.weather.data.repository.WeatherRepository
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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
    var busy = MutableLiveData<Boolean>(false)
    var hasQuery = MutableLiveData<Boolean>(false)

    private val autocompleteFlow = searchQuery.flatMapLatest {
        busy.value = true
        hasQuery.value = it.isNotEmpty()
        repository.getPredictions(it)
    }

    val predictions = autocompleteFlow.asLiveData()

    private val observer = Observer<List<PlacesPrediction>> {
        viewModelScope.launch {
            val size = repository.getCount()
            busy.value = size == 0
        }
    }

    init {
        predictions.observeForever(observer)
    }

    suspend fun onAddPlace(place: PlacesPrediction, added: Boolean) {
        viewModelScope.launch {
            repository.update(place, added)
            if (added) weatherRepository.addWeather(place)
            else weatherRepository.delete(place)
            sendEvent(searchQuery.value)
        }
    }

    fun onDestroy() = viewModelScope.launch {repository.clearDb()}

    fun forceMessage() = viewModelScope.launch {
        sendEvent(searchQuery.value)
    }

    private suspend fun sendEvent(query: String) {
        eventChannel.send(UpdateEvent.UpdateList(repository.getPredictionsAsList(query)))
    }

    private val eventChannel = Channel<UpdateEvent>()
    val event = eventChannel.receiveAsFlow()

    sealed class UpdateEvent {
        data class UpdateList(val list: List<PlacesPrediction>) : UpdateEvent()
    }

    override fun onCleared() {
        predictions.removeObserver(observer)
        super.onCleared()
    }
}