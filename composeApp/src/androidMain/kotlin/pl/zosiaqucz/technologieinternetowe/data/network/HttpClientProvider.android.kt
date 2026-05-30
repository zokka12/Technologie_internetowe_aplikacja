package pl.zosiaqucz.technologieinternetowe.data.network


import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun provideEngine(): HttpClientEngineFactory<*> = OkHttp

actual fun HttpClientConfig<*>.platformConfig() {
    // Android używa pliku security-config.xml skonfigurowanego w sekcji res
}