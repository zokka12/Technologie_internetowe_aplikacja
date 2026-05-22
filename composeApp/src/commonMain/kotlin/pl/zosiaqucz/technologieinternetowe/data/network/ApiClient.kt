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
        return client.get("http://10.0.2.2:8080/cities").body()
    }

    suspend fun addCity(cityName: String, imageUrl: String, attractions: Double, safety: Double, food: Double): Boolean {
        return try {
            val response = client.post("http://10.0.2.2:8080/cities") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer TajnaGeodezja2026")

                // Wysyłamy konkretnie to:
                setBody(CityRequest(cityName, imageUrl, attractions, safety, food, "NONE"))
            }
            // Jeśli serwer zwróci 201 lub 200, jest sukces
            response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
        } catch (e: Exception) {
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

    // Funkcja do aktualizacji ocen i statusu z poziomu suwaków i checkboxów
    suspend fun updateCityDetails(
        id: String,
        attractions: Double? = null,
        safety: Double? = null,
        food: Double? = null,
        status: String? = null
    ): Boolean {
        return try {
            // Ścieżka z ID miasta na końcu (np. /cities/1)
            val response = client.patch("http://$SERVER_IP:8080/cities/$id") {
                contentType(ContentType.Application.Json)
                // 🛡️ BRAMKA BEZPIECZEŃSTWA
                header("Authorization", "Bearer TajnaGeodezja2026")
                // Pakujemy tylko te dane, które użytkownik zmienił
                setBody(CityUpdateRequest(attractions, safety, food, status))
            }
            response.status == HttpStatusCode.OK
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

@Serializable
data class CityUpdateRequest(
    val attractionsRating: Double? = null,
    val safetyRating: Double? = null,
    val foodRating: Double? = null,
    val status: String? = null
)