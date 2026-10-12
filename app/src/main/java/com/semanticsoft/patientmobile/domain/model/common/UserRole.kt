package com.semanticsoft.patientmobile.domain.model.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UserRoleSerializer::class)
enum class UserRole {
    PATIENT,
    LABORATOR,
    UNKNOWN;

    val displayLabel: String
        get() = when (this) {
            PATIENT -> "Pacient"
            LABORATOR -> "Laborator"
            UNKNOWN -> ""
        }

    companion object {
        fun fromRaw(raw: String?): UserRole = when (raw?.trim()?.uppercase()) {
            "PATIENT" -> PATIENT
            "LABORATOR" -> LABORATOR
            else -> UNKNOWN
        }
    }
}

object UserRoleSerializer : KSerializer<UserRole> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserRole", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserRole) =
        encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): UserRole =
        UserRole.fromRaw(decoder.decodeString())
}
