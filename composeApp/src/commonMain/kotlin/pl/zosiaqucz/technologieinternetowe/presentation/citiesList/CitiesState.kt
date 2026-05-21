package pl.zosiaqucz.technologieinternetowe.presentation.citiesList

import pl.zosiaqucz.technologieinternetowe.data.network.CityDto

// Nasza klasa z Wykładu 5! Ogranicza liczbę możliwych scenariuszy.
sealed class CitiesState {

    // Opcja 1: Trwa pobieranie danych (idealne, by pokazać kręcące się kółko)
    data object Loading : CitiesState()

    // Opcja 2: Sukces! Przyszła paczka z miastami z serwera
    data class Success(val cities: List<CityDto>) : CitiesState()

    // Opcja 3: Coś poszło nie tak (np. serwer wyłączony)
    data class Error(val message: String) : CitiesState()
}