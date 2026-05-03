package com.semanticsoft.patientmobile.di

import android.content.Context
import com.semanticsoft.patientmobile.BuildConfig
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.interceptors.AuthInterceptor
import com.semanticsoft.patientmobile.data.remote.interceptors.ErrorInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDate
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(@ApplicationContext context: Context): AuthInterceptor = AuthInterceptor(context)

    @Provides
    @Singleton
    fun provideErrorInterceptor(): ErrorInterceptor = ErrorInterceptor()

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val localDateSerializer = JsonSerializer<LocalDate> { src: LocalDate?, _: Type?, _: JsonSerializationContext? ->
            JsonPrimitive(src?.toString())
        }
        val localDateDeserializer = JsonDeserializer { json: JsonElement, _: Type, _: JsonDeserializationContext? ->
            LocalDate.parse(json.asString)
        }

        val instantSerializer = JsonSerializer<Instant> { src: Instant?, _: Type?, _: JsonSerializationContext? ->
            JsonPrimitive(src?.toString())
        }
        val instantDeserializer = JsonDeserializer { json: JsonElement, _: Type, _: JsonDeserializationContext? ->
            Instant.parse(json.asString)
        }

        return GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, localDateSerializer)
            .registerTypeAdapter(LocalDate::class.java, localDateDeserializer)
            .registerTypeAdapter(Instant::class.java, instantSerializer)
            .registerTypeAdapter(Instant::class.java, instantDeserializer)
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor
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
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun providePatientApiService(retrofit: Retrofit): PatientApiService {
        return retrofit.create(PatientApiService::class.java)
    }
}
