package pl.zosiaqucz.server.application

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import pl.zosiaqucz.server.infrastructure.DatabaseFactory
import kotlin.time.Duration.Companion.seconds


import org.koin.ktor.plugin.Koin
import org.koin.ktor.ext.inject
import pl.zosiaqucz.server.di.appModule
import pl.zosiaqucz.server.domain.UpdateCityUseCase

@Serializable
data class ServerCity(
    val id: String,
    val cityName: String,
    val imageUrl: String,
    val attractionsRating: Double,
    val safetyRating: Double,
    val foodRating: Double,
    val status: String // NOWOŚĆ: np. "VISITED", "TO_SEE"
)

@Serializable
data class CityRequest(
    val cityName: String,
    val imageUrl: String,
    val attractionsRating: Double,
    val safetyRating: Double,
    val foodRating: Double,
    val status: String = "NONE" // Podczas dodawania od razu ustalamy status
)


@Serializable
data class CityUpdateRequest(
    val attractionsRating: Double? = null,
    val safetyRating: Double? = null,
    val foodRating: Double? = null,
    val status: String? = null
)

fun main() {
    // 1. Inicjalizacja bazy danych
    DatabaseFactory.init()

    // 2. Konfiguracja i start serwera na porcie 8080
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        install(Koin) {
            modules(appModule)
        }

        install(ContentNegotiation) {
            json()
        }

        // 3. TARCZA OCHRONNA: Instalujemy limitowanie zapytań (Rate Limiting z Wykładu 7)
        install(RateLimit) {
            register(RateLimitName("ochrona_bazy")) {
                // Ustawiamy rygorystyczny limit: 3 zgłoszenia na 60 sekund
                rateLimiter(limit = 3, refillPeriod = 60.seconds)
            }
        }


        val updateCityUseCase by inject<UpdateCityUseCase>()

        routing {
            // Wstrzykujemy trasy, przekazując gotowe narzędzie
            cityRouting(updateCityUseCase)
        }
    }.start(wait = true)
}