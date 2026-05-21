package pl.zosiaqucz.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ServerCity(
    val id: String,
    val cityName: String,
    val imageUrl: String,
    val attractionsRating: Double,
    val safetyRating: Double,
    val foodRating: Double
)

@Serializable
data class CityRequest(
    val cityName: String,
    val imageUrl: String,
    val attractionsRating: Double,
    val safetyRating: Double,
    val foodRating: Double
)

fun main() {
    // 1. Inicjalizacja bazy danych
    DatabaseFactory.init()

    // 2. Konfiguracja i start serwera na porcie 8080
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        // 3. TARCZA OCHRONNA: Instalujemy limitowanie zapytań!
        install(RateLimit) {
            register(RateLimitName("ochrona_bazy")) {
                // Ustawiamy rygorystyczny limit: 3 zgłoszenia na 60 sekund
                rateLimiter(limit = 3, refillPeriod = 60.seconds)
            }
        }

        routing {
            // Wstrzykujemy trasy, które oddelegowaliśmy do pliku CityRoutes.kt
            cityRouting()
        }
    }.start(wait = true)
}