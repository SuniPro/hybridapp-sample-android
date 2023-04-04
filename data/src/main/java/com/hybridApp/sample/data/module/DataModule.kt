package com.hybridApp.sample.data.module

import android.util.Log
import com.hybridApp.sample.data.BuildConfig
import com.hybridApp.sample.data.datasource.BelleForetMenuRemoteDataSource
import com.hybridApp.sample.data.datasource.PreferenceDataSource
import com.hybridApp.sample.data.datasource.RemoteDataSource
import com.hybridApp.sample.data.datasource.WeatherRemoteDataSource
import com.hybridApp.sample.data.repository.MenuRepositoryImpl
import com.hybridApp.sample.data.repository.UserRepository
import com.hybridApp.sample.data.repository.WeatherRepositoryImpl
import com.hybridApp.sample.data.service.BelleForetMenuService
import com.hybridApp.sample.data.service.KtorBelleForetMenuService
import com.hybridApp.sample.data.service.KtorWeatherService
import com.hybridApp.sample.data.service.WeatherService
import com.hybridApp.sample.domain.repository.MenuRepository
import com.hybridApp.sample.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.http.*
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Singleton
    @Provides
    @Named("belleforet")
    fun provideHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            defaultRequest {
                url {
                    protocol =
                        if (BuildConfig.FLAVOR == "blackstonebelleforet" || BuildConfig.FLAVOR == "domain") {
                            URLProtocol.HTTPS
                        } else {
                            URLProtocol.HTTP
                        }
                    host = BuildConfig.HOST
                }
            }
            install(JsonFeature) {
                GsonSerializer()
            }
            install(Logging) {
                //logger = Logger.DEFAULT
                logger = object : Logger {
                    override fun log(message: String) {
                        if (BuildConfig.DEBUG) {
                            Log.d("Ktor => ", message)
                        }
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    @Singleton
    @Provides
    @Named("weather-service")
    fun provideWeatherHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            defaultRequest {
                /*url {
                    protocol = URLProtocol.HTTPS
                    host = "www.kma.go.kr"
                }*/
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            if (BuildConfig.DEBUG) {
                                Log.d("Ktor => ", message)
                            }
                        }
                    }
                    level = LogLevel.ALL
                }
            }
        }
    }

    @Singleton
    @Provides
    fun provideBelleForetMenuService(
        @Named("belleforet")
        httpClient: HttpClient
    ): BelleForetMenuService {
        return KtorBelleForetMenuService(httpClient)
    }

    @Singleton
    @Provides
    fun provideWeatherService(
        @Named("weather-service")
        httpClient: HttpClient
    ): WeatherService {
        return KtorWeatherService(httpClient)
    }

    @Singleton
    @Provides
    @Named("menu")
    fun provideBelleForetMenuRemoteDataSource(
        belleForetMenuService: BelleForetMenuService
    ): RemoteDataSource {
        return BelleForetMenuRemoteDataSource(belleForetMenuService)
    }

    @Singleton
    @Provides
    @Named("weather")
    fun provideWeatherRemoteDataSource(
        weatherService: WeatherService
    ): RemoteDataSource {
        return WeatherRemoteDataSource(weatherService)
    }

    @Singleton
    @Provides
    fun provideMenuRepository(
        @Named("menu-datasource") preferenceDataSource: PreferenceDataSource,
        @Named("menu") remoteDataSource: RemoteDataSource
    ): MenuRepository {
        return MenuRepositoryImpl(preferenceDataSource, remoteDataSource)
    }

    @Singleton
    @Provides
    fun provideWeatherRepository(
        @Named("weather") remoteDataSource: RemoteDataSource
    ): WeatherRepository {
        return WeatherRepositoryImpl(remoteDataSource)
    }

    @Singleton
    @Provides
    fun provideUserRepository(
        @Named("user-datasource") preferenceDataSource: PreferenceDataSource
    ): UserRepository {
        return UserRepository(preferenceDataSource)
    }
}