package pl.zosiaqucz.server.infrastructure // Upewnij się, że paczka się zgadza

import pl.zosiaqucz.server.domain.CityRepository
import pl.zosiaqucz.server.application.ServerCity // Twój model miasta

// Ta klasa "dziedziczy" (implementuje) kontrakt z warstwy domain
class CityRepositoryImpl : CityRepository {

    override suspend fun getAllCities(): List<ServerCity> {
        // Zamiast wymyślać koło na nowo, wywołujemy Twoją gotową bazę danych!
        return DatabaseFactory.getAllCities()
    }

    override suspend fun updateCityDetails(id: String, attractions: Double, safety: Double, food: Double, status: String): Boolean {
        // Przekazujemy nowe stopnie i oceny z warstwy domain prosto do SQLite
        return DatabaseFactory.updateCity(id, attractions, safety, food, status)
    }
}