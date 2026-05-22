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
import pl.zosiaqucz.server.domain.UpdateCityUseCase
import java.io.File

fun Route.cityRouting(updateCityUseCase: UpdateCityUseCase) {

    get("/") {
        call.respondText("API Geodezyjne z architekturą warstwową działa!")
    }

    // Serwowanie zdjęć
    staticFiles("/", File("C:\\miasta"))

    // ODCZYT (Wyraźny kod 200 OK)
    get("/cities") {
        val citiesFromDb = DatabaseFactory.getAllCities()
        call.respond(HttpStatusCode.OK, citiesFromDb)
    }

    // ZAPIS: Odbieranie nowych punktów pomiarowych
    rateLimit(RateLimitName("ochrona_bazy")) {
        post("/cities") {
            val authHeader = call.request.headers["Authorization"]

            if (authHeader != "Bearer TajnaGeodezja2026") {
                // 🔴 401 Brak dostępu (krótko i na temat)
                call.respond(HttpStatusCode.Unauthorized)
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

            // 🟢 201 Created (System zapisał dane poprawnie)
            call.respond(HttpStatusCode.Created)
        }
    }

    // AKTUALIZACJA: Zmiana stopni i ocen terenu
    patch("/cities/{id}") {
        // 1. Sprawdzamy legitymację bezpieczeństwa
        val authHeader = call.request.headers["Authorization"]
        if (authHeader != "Bearer TajnaGeodezja2026") {
            call.respond(HttpStatusCode.Unauthorized) // 🔴 401
            return@patch
        }

        // 2. Pobieramy ID punktu z adresu URL
        val cityId = call.parameters["id"]
        if (cityId == null) {
            // 🟡 422 Unprocessable Entity - system odrzuca niekompletne dane (brak ID)
            call.respond(HttpStatusCode.UnprocessableEntity)
            return@patch
        }

        // 3. Odbieramy tylko te pola, które chcemy zaktualizować
        val updateData = call.receive<CityUpdateRequest>()

        // 4. Przekazujemy zmiany do czystego Use Case'a
        val success = updateCityUseCase.execute(
            id = cityId,
            attractions = updateData.attractionsRating ?: 0.0,
            safety = updateData.safetyRating ?: 0.0,
            food = updateData.foodRating ?: 0.0,
            status = updateData.status ?: "NONE"
        )

        // 5. Profesjonalna odpowiedź HTTP
        if (success) {
            // 🟢 204 No Content - Zaktualizowano, nie ma potrzeby wysyłania tekstu zwrotnego
            call.respond(HttpStatusCode.NoContent)
        } else {
            // 🔴 404 Not Found - Nie znaleziono punktu w rejestrze
            call.respond(HttpStatusCode.NotFound)
        }
    }
}