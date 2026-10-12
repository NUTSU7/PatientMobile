package com.semanticsoft.patientmobile.core.network.api

import com.semanticsoft.patientmobile.core.network.dto.shared.SharedLinkDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SharedApiService {

    @GET("shared/{token}")
    suspend fun consumeSharedLink(
        @Path("token") token: String,
        @Query("passphrase") passphrase: String? = null
    ): SharedLinkDto
}
