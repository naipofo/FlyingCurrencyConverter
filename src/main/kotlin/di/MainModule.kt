package di

import data.nbpapi.responses.NbpApi
import data.nbpapi.responses.NbpRepository
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val mainModule = DI {
    bindSingleton {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    bindSingleton { NbpApi(instance()) }

    bindSingleton { NbpRepository(instance()) }
}