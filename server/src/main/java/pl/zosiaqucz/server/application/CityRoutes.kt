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
import io.ktor.server.routing.route
import pl.zosiaqucz.server.infrastructure.DatabaseFactory
import pl.zosiaqucz.server.domain.UpdateCityUseCase
import java.io.File

fun Route.cityRouting(updateCityUseCase: UpdateCityUseCase) {

    get("/") {
        call.respondText("API Geodezyjne z architekturą warstwową (v1) działa!")
    }

    // Serwowanie zdjęć
    staticFiles("/", File("C:\\miasta"))

    // 🏗️ NOWOŚĆ: Grupujemy wszystkie ścieżki pod parasolem /v1
    route("/v1") {

        // ODCZYT (Wyraźny kod 200 OK) -> Adres: /v1/cities
        get("/cities") {
            val citiesFromDb = DatabaseFactory.getAllCities()
            call.respond(HttpStatusCode.OK, citiesFromDb)
        }

        // ZAPIS: Odbieranie nowych punktów pomiarowych -> Adres: /v1/cities
        rateLimit(RateLimitName("ochrona_bazy")) {
            post("/cities") {
                val authHeader = call.request.headers["Authorization"]

                if (authHeader != "Bearer TajnaGeodezja2026") {
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

                call.respond(HttpStatusCode.Created)
            }
        }

        // AKTUALIZACJA: Zmiana stopni i ocen terenu -> Adres: /v1/cities/{id}
        patch("/cities/{id}") {
            val authHeader = call.request.headers["Authorization"]
            if (authHeader != "Bearer TajnaGeodezja2026") {
                call.respond(HttpStatusCode.Unauthorized)
                return@patch
            }

            val cityId = call.parameters["id"]
            if (cityId == null) {
                call.respond(HttpStatusCode.UnprocessableEntity)
                return@patch
            }

            val updateData = call.receive<CityUpdateRequest>()

            val success = updateCityUseCase.execute(
                id = cityId,
                attractions = updateData.attractionsRating ?: 0.0,
                safety = updateData.safetyRating ?: 0.0,
                food = updateData.foodRating ?: 0.0,
                status = updateData.status ?: "NONE"
            )

            if (success) {
                // 🟢 204 No Content - Zaktualizowano stopnie pomyślnie
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}