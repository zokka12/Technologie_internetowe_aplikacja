package pl.zosiaqucz.technologieinternetowe.data.network


import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


const val HOST = "192.168.X.X"
const val PORT = 8080

expect fun provideEngine(): HttpClientEngineFactory<*>
expect fun HttpClientConfig<*>.platformConfig()

object HttpClientProvider {
    val httpClient: HttpClient by lazy {
        HttpClient(provideEngine()) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTP
                    host = HOST
                    port = PORT
                }
            }

            platformConfig()
        }
    }
}