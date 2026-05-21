package com.semanticsoft.patientmobile.domain.repository

import com.semanticsoft.patientmobile.domain.model.SharedLink
import com.semanticsoft.patientmobile.util.ApiResult

interface SharedLinkRepository {
    suspend fun consumeSharedLink(token: String, passphrase: String? = null): ApiResult<SharedLink>
}
