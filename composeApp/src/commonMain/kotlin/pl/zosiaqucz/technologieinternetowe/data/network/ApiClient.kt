package pl.zosiaqucz.technologieinternetowe.data.network

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
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun fetchCities(): List<CityDto> {
        return client.get("http://10.0.2.2:8080/cities").body()
    }

    suspend fun addCity(cityName: String, imageUrl: String, attractions: Double, safety: Double, food: Double): Boolean {
        return try {
            val response = client.post("http://10.0.2.2:8080/cities") {
                contentType(ContentType.Application.Json)

                // KLUCZOWA ZMIANA: Dołączamy elektroniczną legitymację (Autoryzację) do wysyłanej paczki!
                header("Authorization", "Bearer TajnaGeodezja2026")

                setBody(CityRequest(cityName, imageUrl, attractions, safety, food))
            }
            response.status == HttpStatusCode.Created
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 4. POBIERANIE WSPÓŁRZĘDNYCH: Zmieniliśmy dostawcę na stabilniejsze Open-Meteo Geocoding!
    suspend fun getCityCoordinates(cityName: String): Pair<String, String>? {
        return try {
            val cleanCityName = cityName.trim()

            // Łączymy się z nowym, odpornym na blokady serwerem
            val response: GeocodingResponse = client.get("https://geocoding-api.open-meteo.com/v1/search") {
                parameter("name", cleanCityName)
                parameter("count", "1")
                parameter("language", "pl")
                parameter("format", "json")
            }.body()

            // Wyciągamy z JSON-a rygorystyczne stopnie
            val results = response.results
            if (results != null && results.isNotEmpty()) {
                Pair(results[0].latitude.toString(), results[0].longitude.toString())
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Serializable
data class CityRequest(
    val cityName: String,
    val imageUrl: String,
    val attractionsRating: Double,
    val safetyRating: Double,
    val foodRating: Double
)

// Nowe klasy pomocnicze dla Open-Meteo API
@Serializable
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

@Serializable
data class GeocodingResult(
    val latitude: Double,
    val longitude: Double
)