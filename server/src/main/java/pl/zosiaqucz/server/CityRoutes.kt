package pl.zosiaqucz.server

import io.ktor.server.routing.patch
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

fun Route.cityRouting() {

    get("/") {
        call.respondText("API z Bazą Danych działa!")
    }

    // Serwowanie zdjęć
    staticFiles("/", File("C:\\miasta"))

    // ODCZYT (Brak limitu - chcemy, żeby telefony mogły swobodnie odczytywać mapę)
    get("/cities") {
        val citiesFromDb = DatabaseFactory.getAllCities()
        call.respond(citiesFromDb)
    }

    // ZAPIS: Odbieranie nowych lokalizacji
    // 🛡️ TARCZA NR 1: Ochrona przed spamem (Rate Limit)
    rateLimit(RateLimitName("ochrona_bazy")) {

        post("/cities") {
            // 🛡️ TARCZA NR 2: BRAMKA BEZPIECZEŃSTWA (Autoryzacja - Elektroniczna Legitymacja)
            val authHeader = call.request.headers["Authorization"]

            if (authHeader != "Bearer TajnaGeodezja2026") {
                call.respond(HttpStatusCode.Unauthorized, "Odmowa dostępu! Błędna legitymacja.")
                return@post
            }

            // Jeśli hasło jest poprawne i limit nie został przekroczony (jesteśmy poniżej 3 prób/minutę)
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

    }
    // <-- Koniec klamry rateLimit
    // AKTUALIZACJA: Zmiana oceny na 10 lub statusu miasta
    patch("/cities/{id}") {
        // 1. Sprawdzamy legitymację bezpieczeństwa
        val authHeader = call.request.headers["Authorization"]
        if (authHeader != "Bearer TajnaGeodezja2026") {
            call.respond(HttpStatusCode.Unauthorized, "Odmowa dostępu!")
            return@patch
        }

        // 2. Pobieramy ID miasta z adresu URL
        val cityId = call.parameters["id"]
        if (cityId == null) {
            call.respond(HttpStatusCode.BadRequest, "Brak ID miasta!")
            return@patch
        }

        // 3. Odbieramy tylko te pola, które chcemy zaktualizować
        val updateData = call.receive<CityUpdateRequest>()

        // 4. Przekazujemy zmiany do bazy danych
        val success = DatabaseFactory.updateCity(
            id = cityId,
            newAttr = updateData.attractionsRating,
            newSafe = updateData.safetyRating,
            newFood = updateData.foodRating,
            newStatus = updateData.status
        )

        if (success) {
            call.respond(HttpStatusCode.OK, "Oceny/status zaktualizowane pomyślnie!")
        } else {
            call.respond(HttpStatusCode.NotFound, "Nie znaleziono miasta o podanym ID.")
        }
    }

}
