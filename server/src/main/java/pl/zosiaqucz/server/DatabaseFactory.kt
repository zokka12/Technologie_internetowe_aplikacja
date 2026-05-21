package pl.zosiaqucz.server

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Cities : Table() {
    val id = integer("id").autoIncrement()
    val cityName = varchar("cityName", 50)
    val imageUrl = varchar("imageUrl", 255)
    val attractionsRating = double("attractionsRating")
    val safetyRating = double("safetyRating")
    val foodRating = double("foodRating")

    // NOWOŚĆ: Dodana kolumna statusu. Domyślna wartość to "NONE" (Brak statusu)
    val status = varchar("status", 50).default("NONE")

    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:sqlite:./data.db", "org.sqlite.JDBC")

        transaction {
            // UWAGA INŻYNIERSKA: Przy pierwszej zmianie struktury tabeli (dodaniu kolumny),
            // stara baza może zgłosić błąd. Zobacz instrukcję pod kodem!
            SchemaUtils.create(Cities)

            if (Cities.selectAll().empty()) {
                addCity("Warszawa", "http://10.0.2.2:8080/warsaw.jpg", 8.5, 9.0, 8.2)
                addCity("Rzym", "http://10.0.2.2:8080/rome.jpg", 9.5, 7.2, 9.6)
                addCity("Paryż", "http://10.0.2.2:8080/paris.jpg", 9.2, 6.8, 8.8)
                addCity("Londyn", "http://10.0.2.2:8080/london.jpg", 8.9, 8.1, 7.9)
                addCity("Berlin", "http://10.0.2.2:8080/berlin.jpg", 8.0, 8.5, 8.3)
                addCity("Amsterdam", "http://10.0.2.2:8080/amsterdam.jpg", 8.8, 9.1, 8.0)
                addCity("Lizbona", "http://10.0.2.2:8080/lisbon.jpg", 9.0, 8.7, 9.3)
                addCity("Wiedeń", "http://10.0.2.2:8080/vienna.jpg", 8.4, 8.2, 9.1)
                addCity("Budapeszt", "http://10.0.2.2:8080/budapest.jpg", 9.1, 7.5, 8.9)
                addCity("Praga", "http://10.0.2.2:8080/prague.jpg", 8.7, 8.8, 8.5)
            }
        }
    }

    // NOWOŚĆ: Dodany parametr cityStatus z domyślną wartością "NONE"
    fun addCity(name: String, url: String, attr: Double, safe: Double, food: Double, cityStatus: String = "NONE") {
        transaction {
            Cities.insert {
                it[cityName] = name
                it[imageUrl] = url
                it[attractionsRating] = attr
                it[safetyRating] = safe
                it[foodRating] = food
                it[status] = cityStatus
            }
        }
    }

    fun getAllCities(): List<ServerCity> = transaction {
        Cities.selectAll().map {
            ServerCity(
                id = it[Cities.id].toString(),
                cityName = it[Cities.cityName],
                imageUrl = it[Cities.imageUrl],
                attractionsRating = it[Cities.attractionsRating],
                safetyRating = it[Cities.safetyRating],
                foodRating = it[Cities.foodRating],
                status = it[Cities.status] // NOWOŚĆ: Mapowanie statusu z bazy do paczki
            )
        }
    }

    // NOWOŚĆ: Funkcja do chirurgicznej aktualizacji ocen i statusów (bramka PATCH)
    fun updateCity(id: String, newAttr: Double?, newSafe: Double?, newFood: Double?, newStatus: String?): Boolean {
        var updatedRows = 0
        transaction {
            // Przerabiamy id ze Stringa na Int, żeby zgadzało się z bazą danych
            updatedRows = Cities.update({ Cities.id eq id.toInt() }) { row ->
                // Modyfikujemy tylko te oceny na 10, które zostały przysłane
                newAttr?.let { row[attractionsRating] = it }
                newSafe?.let { row[safetyRating] = it }
                newFood?.let { row[foodRating] = it }
                newStatus?.let { row[status] = it }
            }
        }
        return updatedRows > 0 // Jeśli zaktualizowano co najmniej 1 wiersz, zwraca true
    }
}