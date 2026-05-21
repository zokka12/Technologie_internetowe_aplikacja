package pl.zosiaqucz.technologieinternetowe.data.network

import kotlinx.serialization.Serializable

@Serializable
data class CityDto(
    val id: String,
    val cityName: String,
    val imageUrl: String,
    val attractionsRating: Double,
    val safetyRating: Double,
    val foodRating: Double,
    val status: String = "NONE"
)