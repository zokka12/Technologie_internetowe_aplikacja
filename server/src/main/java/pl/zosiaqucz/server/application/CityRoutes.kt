package pl.zosiaqucz.server.application

import io.ktor.server.routing.patch
import io.ktor.http.HttpStatusCode
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import pl.zosiaqucz.server.infrastructure.DatabaseFactory
import pl.zosiaqucz.server.domain.UpdateCityUseCase // 🔴 DODANY IMPORT (upewnij się, że paczka się zgadza!)
import java.io.File

// 🔴 ZMIANA 1: Nasz routing wymaga teraz narzędzia UpdateCityUseCase
fun Route.cityRouting(updateCityUseCase: UpdateCityUseCase) {

    get("/") {
        call.respondText("API z Bazą Danych działa!")
    }

    // Serwowanie zdjęć
    staticFiles("/", File("C:\\miasta"))

    // ODCZYT (Na razie zostawiamy tutaj stare zapytanie, potem to też "wyczyścimy")
    get("/cities") {
        val citiesFromDb = DatabaseFactory.getAllCities()
        call.respond(citiesFromDb)
    }

    // ZAPIS
    rateLimit(RateLimitName("ochrona_bazy")) {
        post("/cities") {
            val authHeader = call.request.headers["Authorization"]

            if (authHeader != "Bearer TajnaGeodezja2026") {
                call.respond(HttpStatusCode.Unauthorized, "Odmowa dostępu! Błędna legitymacja.")
                return@post
            }

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

        // 🔴 ZMIANA 2: ZAMIAST DATABASEFACTORY, UŻYWAMY NASZEGO CZYSTEGO USE CASE'A!
        // Używamy "Elvis operatora" (?:), żeby upewnić się, że nie wysyłamy pustych wartości (null) do funkcji
        val success = updateCityUseCase.execute(
            id = cityId,
            attractions = updateData.attractionsRating ?: 0.0,
            safety = updateData.safetyRating ?: 0.0,
            food = updateData.foodRating ?: 0.0,
            status = updateData.status ?: "NONE"
        )

        if (success) {
            call.respond(HttpStatusCode.OK, "Oceny/status zaktualizowane pomyślnie!")
        } else {
            call.respond(HttpStatusCode.NotFound, "Nie znaleziono miasta o podanym ID.")
        }
    }
}