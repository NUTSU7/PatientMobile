package com.semanticsoft.patientmobile.data.remote.api.dto.mappers

import com.semanticsoft.patientmobile.data.remote.api.dto.auth.AuthResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.auth.RefreshResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.auth.UserDto
import com.semanticsoft.patientmobile.domain.model.AuthResponse as DomainAuthResponse
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.model.UserRole

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
