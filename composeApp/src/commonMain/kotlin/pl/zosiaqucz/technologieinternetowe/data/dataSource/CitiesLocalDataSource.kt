package pl.zosiaqucz.technologieinternetowe.data.dataSource

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.zosiaqucz.technologieinternetowe.domain.model.EuropeanCity

object CitiesLocalDataSource {
    private val _cities = MutableStateFlow(
        listOf(
            EuropeanCity(
                id = "warsaw",
                cityName = "Warszawa",
                attractionsRating = 8.5,
                safetyRating = 9.0,
                foodRating = 8.0,
                mapUrl = "https://maps.app.goo.gl/warszawa"
            ),
            EuropeanCity(
                id = "paris",
                cityName = "Paryż",
                attractionsRating = 9.5,
                safetyRating = 6.5,
                foodRating = 9.0,
                mapUrl = "https://maps.app.goo.gl/paryz",
                isVisited = true
            ),
            EuropeanCity(
                id = "rome",
                cityName = "Rzym",
                attractionsRating = 9.8,
                safetyRating = 7.0,
                foodRating = 9.5,
                mapUrl = "https://maps.app.goo.gl/rzym"
            ),
            EuropeanCity(
                id = "lisbon",
                cityName = "Lizbona",
                attractionsRating = 8.8,
                safetyRating = 8.5,
                foodRating = 9.2,
                mapUrl = "https://maps.app.goo.gl/lizbona"
            ),
            EuropeanCity(
                id = "london",
                cityName = "Londyn",
                attractionsRating = 9.2,
                safetyRating = 7.5,
                foodRating = 8.5,
                mapUrl = "https://maps.app.goo.gl/londyn"
            ),
            EuropeanCity(
                id = "berlin",
                cityName = "Berlin",
                attractionsRating = 8.0,
                safetyRating = 8.5,
                foodRating = 8.2,
                mapUrl = "https://maps.app.goo.gl/berlin"
            ),
            EuropeanCity(
                id = "prague",
                cityName = "Praga",
                attractionsRating = 9.0,
                safetyRating = 8.0,
                foodRating = 8.8,
                mapUrl = "https://maps.app.goo.gl/praga"
            ),
            EuropeanCity(
                id = "vienna",
                cityName = "Wiedeń",
                attractionsRating = 8.5,
                safetyRating = 9.5,
                foodRating = 8.5,
                mapUrl = "https://maps.app.goo.gl/wieden"
            ),
            EuropeanCity(
                id = "budapest",
                cityName = "Budapeszt",
                attractionsRating = 8.7,
                safetyRating = 8.0,
                foodRating = 8.4,
                mapUrl = "https://maps.app.goo.gl/budapeszt"
            ),
            EuropeanCity(
                id = "amsterdam",
                cityName = "Amsterdam",
                attractionsRating = 8.9,
                safetyRating = 8.8,
                foodRating = 8.0,
                mapUrl = "https://maps.app.goo.gl/amsterdam"
            )
        )
    )

    val cities: StateFlow<List<EuropeanCity>> = _cities.asStateFlow()

    // 1. Funkcja do oznaczania odwiedzonych miejsc (tej pewnie brakuje!)
    fun toggleCityVisited(cityId: String) {
        _cities.update { citiesList ->
            citiesList.map { city ->
                if (city.id == cityId) {
                    city.copy(isVisited = !city.isVisited)
                } else {
                    city
                }
            }
        }
    }

    // 2. Funkcja do oznaczania "Chcę odwiedzić"
    fun toggleCityToVisit(cityId: String) {
        _cities.update { citiesList ->
            citiesList.map { city ->
                if (city.id == cityId) {
                    city.copy(isToVisit = !city.isToVisit)
                } else {
                    city
                }
            }
        }
    }

    // 3. Funkcja do aktualizacji suwaka z oceną
    fun updateCityUserRating(cityId: String, newRating: Double) {
        _cities.update { citiesList ->
            citiesList.map { city ->
                if (city.id == cityId) {
                    city.copy(userRating = newRating)
                } else {
                    city
                }
            }
        }
    }
} // <- Tu jest koniec pliku

