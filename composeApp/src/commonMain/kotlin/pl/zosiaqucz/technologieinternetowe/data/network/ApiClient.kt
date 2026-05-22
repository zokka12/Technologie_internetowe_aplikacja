package pl.zosiaqucz.technologieinternetowe.data.network

import io.ktor.client.request.patch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object ApiClient {
    private const val SERVER_IP = "10.0.2.2"
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun fetchCities(): List<CityDto> {
        // 🔴 ZMIANA: Pobieranie z v1
        return client.get("http://$SERVER_IP:8080/v1/cities").body()
    }

    suspend fun addCity(cityName: String, imageUrl: String, attractions: Double, safety: Double, food: Double): Boolean {
        return try {
            // 🔴 ZMIANA: Wysyłanie do v1
            val response = client.post("http://$SERVER_IP:8080/v1/cities") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer TajnaGeodezja2026")
                setBody(CityRequest(cityName, imageUrl, attractions, safety, food, "NONE"))
            }
            response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    // POBIERANIE WSPÓŁRZĘDNYCH (Zostaje bez zmian, używa API zewnętrznego)
    suspend fun getCityCoordinates(cityName: String): Pair<String, String>? {
        return try {
            val cleanCityName = cityName.trim()
            val response: GeocodingResponse = client.get("https://geocoding-api.open-meteo.com/v1/search") {
                parameter("name", cleanCityName)
                parameter("count", "1")
                parameter("language", "pl")
                parameter("format", "json")
            }.body()

            val results = response.results
            if (results != null && results.isNotEmpty()) {
                Pair(results[0].latitude.toString(), results[0].longitude.toString())
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // AKTUALIZACJA STOPNI (PATCH)
    suspend fun updateCityDetails(
        id: String,
        attractions: Double? = null,
        safety: Double? = null,
        food: Double? = null,
        status: String? = null
    ): Boolean {
        return try {
            // 🔴 ZMIANA: Ścieżka z /v1/
            val response = client.patch("http://$SERVER_IP:8080/v1/cities/$id") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer TajnaGeodezja2026")
                setBody(CityUpdateRequest(attractions, safety, food, status))
            }
            // 🟢 ZMIANA: Obsługa kodu HttpStatusCode.NoContent (204) zwracanego przez serwer
            response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@Serializable
data class CityRequest(
    val cityName: String,
    val imageUrl: String,
    val attractionsRating: Double,
    val safetyRating: Double,
    val foodRating: Double,
    val status: String = "NONE"
)

@Serializable
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

@Serializable
data class GeocodingResult(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class CityUpdateRequest(
    val attractionsRating: Double? = null,
    val safetyRating: Double? = null,
    val foodRating: Double? = null,
    val status: String? = null
)