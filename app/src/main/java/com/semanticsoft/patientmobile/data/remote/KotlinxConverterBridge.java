package com.semanticsoft.patientmobile.data.remote;

import com.jakewharton.retrofit2.converter.kotlinx.serialization.KotlinSerializationConverterFactory;
import kotlinx.serialization.json.Json;
import okhttp3.MediaType;
import retrofit2.Converter;

public final class KotlinxConverterBridge {
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private KotlinxConverterBridge() {}

    public static Converter.Factory createFactory(Json json) {
        return KotlinSerializationConverterFactory.create(json, JSON_MEDIA_TYPE);
    }
}
