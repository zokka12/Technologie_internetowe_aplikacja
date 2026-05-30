package pl.zosiaqucz.technologieinternetowe.data.network


import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging

actual fun provideEngine(): HttpClientEngineFactory<*> = OkHttp

actual fun HttpClientConfig<*>.platformConfig() {
    // Wymóg rejestracji logowania na poziomie INFO
    install(Logging) {
        level = LogLevel.INFO
    }
}