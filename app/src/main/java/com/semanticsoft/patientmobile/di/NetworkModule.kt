package com.semanticsoft.patientmobile.di

import android.content.Context
import com.semanticsoft.patientmobile.BuildConfig
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.interceptors.AuthInterceptor
import com.semanticsoft.patientmobile.data.remote.interceptors.ErrorInterceptor
import com.semanticsoft.patientmobile.data.remote.interceptors.TokenRefreshAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.semanticsoft.patientmobile.data.remote.KotlinxConverterBridge

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(@ApplicationContext context: Context): AuthInterceptor =
        AuthInterceptor(context)

    @Provides
    @Singleton
    fun provideErrorInterceptor(): ErrorInterceptor = ErrorInterceptor()

    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenRefreshAuthenticator(
        @ApplicationContext context: Context,
        @Named("refresh") refreshOkHttpClient: OkHttpClient,
        json: Json
    ): TokenRefreshAuthenticator = TokenRefreshAuthenticator(context, refreshOkHttpClient, json)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(logging)
            .authenticator(tokenRefreshAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(KotlinxConverterBridge.createFactory(json))
            .build()
    }

    @Provides
    @Singleton
    fun providePatientApiService(retrofit: Retrofit): PatientApiService {
        return retrofit.create(PatientApiService::class.java)
    }
}
