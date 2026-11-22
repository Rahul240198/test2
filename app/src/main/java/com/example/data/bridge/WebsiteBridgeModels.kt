package com.example.data.bridge

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthResponse(
  val status: String = "ok",
  val parish: String? = null,
  val version: String? = null,
)

@JsonClass(generateAdapter = true)
data class MobileLoginRequest(
  val mobileNumber: String,
  val parishId: String? = null,
)

@JsonClass(generateAdapter = true)
data class MobileLoginResponse(
  val success: Boolean = true,
  val token: String? = null,
  val message: String? = null,
  val parishioner: ParishionerDto? = null,
  val requests: List<CertificateRequestDto>? = null,
)

@JsonClass(generateAdapter = true)
data class ParishionerDto(
  val userId: String? = null,
  val parishId: String? = null,
  val fullName: String? = null,
  val email: String? = null,
  val phone: String? = null,
  val address: String? = null,
  val sccUnit: String? = null,
  val headOfFamily: String? = null,
  val dateOfBirth: String? = null,
  val baptismDate: String? = null,
  val spouseName: String? = null,
)

@JsonClass(generateAdapter = true)
data class ParishionerSyncPayload(
  val userId: String,
  val parishId: String,
  val fullName: String,
  val email: String,
  val phone: String,
  val address: String,
  val sccUnit: String,
  val headOfFamily: String,
  val dateOfBirth: String,
  val baptismDate: String,
  val spouseName: String,
)

@JsonClass(generateAdapter = true)
data class CertificateRequestDto(
  val requestId: String? = null,
  val certificateType: String? = null,
  val applicantName: String? = null,
  val subjectName: String? = null,
  val eventDate: String? = null,
  val placeOrParish: String? = null,
  val parentsOrSpouseNames: String? = null,
  val sponsorOrNextOfKin: String? = null,
  val registryReference: String? = null,
  val purpose: String? = null,
  val notes: String? = null,
  val status: String? = null,
  val createdAtMillis: Long? = null,
)

@JsonClass(generateAdapter = true)
data class CertificateSyncPayload(
  val requestId: String,
  val userId: String,
  val certificateType: String,
  val applicantName: String,
  val subjectName: String,
  val eventDate: String,
  val placeOrParish: String,
  val parentsOrSpouseNames: String,
  val sponsorOrNextOfKin: String,
  val registryReference: String,
  val purpose: String,
  val notes: String,
  val status: String,
)

@JsonClass(generateAdapter = true)
data class ApiSyncResponse(
  val success: Boolean = true,
  val message: String? = null,
  val referenceCode: String? = null,
)

data class UserSession(
  val isLoggedIn: Boolean = false,
  val userId: String = "",
  val mobileNumber: String = "",
  val parishId: String = "",
  val fullName: String = "",
  val authToken: String = "",
  val isDummyMode: Boolean = false,
)

data class WebsiteBridgeConfig(
  val baseUrl: String = "",
  val apiToken: String = "",
  val autoSyncEnabled: Boolean = true,
  val lastSyncMessage: String = "Configure your parish website API URL or use Dummy Test Mode",
  val lastHealthOk: Boolean? = null,
)

sealed interface BridgeSyncOutcome {
  data class Synced(val message: String) : BridgeSyncOutcome
  data object NotConfigured : BridgeSyncOutcome
  data class Failed(val errorMessage: String) : BridgeSyncOutcome
}
