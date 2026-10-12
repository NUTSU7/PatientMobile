package com.semanticsoft.patientmobile.domain.repository.patient

import com.semanticsoft.patientmobile.domain.model.patient.SharedLink
import com.semanticsoft.patientmobile.core.util.ApiResult

interface SharedLinkRepository {
    suspend fun consumeSharedLink(token: String, passphrase: String? = null): ApiResult<SharedLink>
}
