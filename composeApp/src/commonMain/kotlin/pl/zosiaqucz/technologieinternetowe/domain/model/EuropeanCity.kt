package pl.zosiaqucz.technologieinternetowe.domain.model


data class EuropeanCity(
    val id: String,
    val cityName: String,
    val attractionsRating: Double,
    val safetyRating: Double,
    val foodRating: Double,
    val mapUrl: String,
    val isVisited: Boolean = false,
    val isToVisit: Boolean = false,
    val userRating: Double = 0.0,
    val imageUrl: String = ""
) {

    val averageRating: Double
        get() = (attractionsRating + safetyRating + foodRating) / 3.0
}