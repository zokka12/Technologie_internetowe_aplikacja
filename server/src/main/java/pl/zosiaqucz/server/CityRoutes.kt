package pl.zosiaqucz.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.io.File

// Przeniesiona w całości logika tras
fun Route.cityRouting() {

    get("/") {
        call.respondText("API z Bazą Danych działa!")
    }

    // Serwowanie zdjęć z dysku C
    staticFiles("/", File("C:\\miasta"))

    // ODCZYT: Wysłanie pełnej bazy do telefonu
    get("/cities") {
        val citiesFromDb = DatabaseFactory.getAllCities()
        call.respond(citiesFromDb)
    }

    // ZAPIS: Odbieranie nowych lokalizacji
    // 🛡️ TARCZA NR 1: Ochrona przed spamem (Rate Limit)
    // Obejmujemy cały blok POST naszą regułą "ochrona_bazy" zdefiniowaną w Server.kt
    rateLimit(RateLimitName("ochrona_bazy")) {

        post("/cities") {
            // 🛡️ TARCZA NR 2: BRAMKA BEZPIECZEŃSTWA (Autoryzacja z Wykładu 7)
            val authHeader = call.request.headers["Authorization"]

            // Sprawdzamy, czy aplikacja przysłała odpowiednią legitymację
            if (authHeader != "Bearer TajnaGeodezja2026") {
                // Jeśli hasło się nie zgadza, odrzucamy intruza statusem 401 Unauthorized!
                call.respond(HttpStatusCode.Unauthorized, "Odmowa dostępu! Błędna legitymacja.")
                return@post // Przerywamy działanie funkcji, kod poniżej się nie wykona
            }

            // Jeśli hasło jest poprawne i limit nie został przekroczony, przepuszczamy paczkę dalej
            val newCity = call.receive<CityRequest>()

            DatabaseFactory.addCity(
                name = newCity.cityName,
                url = newCity.imageUrl,
                attr = newCity.attractionsRating,
                safe = newCity.safetyRating,
                food = newCity.foodRating
            )

            call.respond(HttpStatusCode.Created, "Zapisano nowe miasto z prawidłowymi ocenami!")
        }

    } // <-- Koniec klamry rateLimit
}