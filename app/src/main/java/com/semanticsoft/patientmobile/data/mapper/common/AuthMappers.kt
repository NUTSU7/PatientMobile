package com.semanticsoft.patientmobile.data.mapper.common

import com.semanticsoft.patientmobile.core.network.dto.auth.AuthResponse
import com.semanticsoft.patientmobile.core.network.dto.auth.RefreshResponse
import com.semanticsoft.patientmobile.core.network.dto.auth.UserDto
import com.semanticsoft.patientmobile.domain.model.common.AuthResponse as DomainAuthResponse
import com.semanticsoft.patientmobile.domain.model.common.User
import com.semanticsoft.patientmobile.domain.model.common.UserRole

fun AuthResponse.toDomain(): DomainAuthResponse = DomainAuthResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = tokenType.orEmpty(),
    expiresIn = expiresIn,
    refreshExpiresIn = refreshExpiresIn,
    user = user.toDomain()
)

fun RefreshResponse.toDomain(): DomainAuthResponse = DomainAuthResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = tokenType.orEmpty(),
    expiresIn = expiresIn,
    refreshExpiresIn = refreshExpiresIn,
    user = user.toDomain()
)

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    dateOfBirth = dateOfBirth?.let(::parseDateSafe),
    role = UserRole.fromRaw(role),
    createdAt = parseInstantSafe(createdAt)
)
