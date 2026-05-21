package pl.zosiaqucz.technologieinternetowe.presentation.citiesList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.zosiaqucz.technologieinternetowe.data.network.ApiClient

class CitiesListViewModel : ViewModel() {

    // Stan ładowania aplikacji (Wykład 5) [cite: 3314, 3316]
    private val _state = MutableStateFlow<CitiesState>(CitiesState.Loading)
    val state: StateFlow<CitiesState> = _state

    init {
        // Przy starcie automatycznie dociągamy miasta z bazy
        fetchCitiesFromNetwork()
    }

    // POPRAWIONE: Usunęliśmy słówko "private". Funkcja jest teraz publiczna!
    // Dzięki temu CitiesListScreen może jej bez problemu użyć do odświeżenia listy po POST-cie.
    fun fetchCitiesFromNetwork() {
        viewModelScope.launch {
            try {
                _state.value = CitiesState.Loading

                // Pobieramy dane z naszego API (Wykład 5 & 6) [cite: 3317, 3422]
                val downloadedCities = ApiClient.fetchCities()
                _state.value = CitiesState.Success(downloadedCities)
            } catch (e: Exception) {
                _state.value = CitiesState.Error("Błąd serwera: ${e.message}")
            }
        }
    }
}