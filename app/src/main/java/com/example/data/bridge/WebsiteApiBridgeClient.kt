package com.example.data.bridge

import com.example.model.CertificateRequest
import com.example.model.ParishionerProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ParishWebsiteApiService {
  @GET("health")
  suspend fun checkHealth(
    @Header("Authorization") authHeader: String?
  ): Response<HealthResponse>

  @POST("auth/mobile-login")
  suspend fun loginWithMobile(
    @Header("Authorization") authHeader: String?,
    @Body request: MobileLoginRequest,
  ): Response<MobileLoginResponse>

  @GET("parishioners/me")
  suspend fun getParishionerProfile(
    @Header("Authorization") authHeader: String?
  ): Response<ParishionerDto>

  @PUT("parishioners/me")
  suspend fun upsertParishionerProfile(
    @Header("Authorization") authHeader: String?,
    @Body payload: ParishionerSyncPayload,
  ): Response<ApiSyncResponse>

  @GET("certificates/requests")
  suspend fun getCertificateRequests(
    @Header("Authorization") authHeader: String?
  ): Response<List<CertificateRequestDto>>

  @POST("certificates/requests")
  suspend fun submitCertificateRequest(
    @Header("Authorization") authHeader: String?,
    @Body payload: CertificateSyncPayload,
  ): Response<ApiSyncResponse>

  @POST("certificates/requests/{id}/cancel")
  suspend fun cancelCertificateRequest(
    @Header("Authorization") authHeader: String?,
    @Path("id") requestId: String,
  ): Response<ApiSyncResponse>
}

class WebsiteApiBridgeClient {

  private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

  private val okHttpClient: OkHttpClient =
    OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .addInterceptor(
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
      )
      .build()

  private fun createService(baseUrl: String): ParishWebsiteApiService {
    val safeUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    return Retrofit.Builder()
      .baseUrl(safeUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(ParishWebsiteApiService::class.java)
  }

  private fun buildAuthHeader(sessionToken: String?, apiToken: String): String? {
    val chosen = sessionToken?.trim()?.takeIf { it.isNotEmpty() } ?: apiToken.trim()
    return chosen.takeIf { it.isNotEmpty() }?.let { "Bearer $it" }
  }

  suspend fun loginWithMobile(
    config: WebsiteBridgeConfig,
    mobileNumber: String,
    parishId: String?,
  ): Result<MobileLoginResponse> =
    withContext(Dispatchers.IO) {
      if (config.baseUrl.isBlank()) {
        return@withContext Result.failure(
          IllegalStateException("Website API URL is not configured yet.")
        )
      }
      try {
        val response =
          createService(config.baseUrl)
            .loginWithMobile(
              authHeader = buildAuthHeader(null, config.apiToken),
              request =
                MobileLoginRequest(
                  mobileNumber = mobileNumber,
                  parishId = parishId?.takeIf { it.isNotBlank() },
                ),
            )
        if (response.isSuccessful) {
          val body = response.body()
          if (body != null && body.success) {
            Result.success(body)
          } else {
            Result.failure(
              IllegalStateException(body?.message ?: "Mobile number not found in parish database.")
            )
          }
        } else {
          Result.failure(
            IllegalStateException("Parish API login failed (HTTP ${response.code()}).")
          )
        }
      } catch (e: Exception) {
        Result.failure(
          IllegalStateException(
            e.localizedMessage ?: "Unable to connect to St. Anthony's Parish API."
          )
        )
      }
    }

  suspend fun testConnection(config: WebsiteBridgeConfig): BridgeSyncOutcome =
    withContext(Dispatchers.IO) {
      if (config.baseUrl.isBlank()) return@withContext BridgeSyncOutcome.NotConfigured
      try {
        val response =
          createService(config.baseUrl).checkHealth(buildAuthHeader(null, config.apiToken))
        if (response.isSuccessful) {
          val parish = response.body()?.parish ?: "St. Anthony's Parish API"
          BridgeSyncOutcome.Synced("Connected to $parish (HTTP ${response.code()})")
        } else {
          BridgeSyncOutcome.Failed("Website API returned HTTP ${response.code()}")
        }
      } catch (e: Exception) {
        BridgeSyncOutcome.Failed(e.localizedMessage ?: "Unable to reach website API")
      }
    }

  suspend fun fetchProfile(
    config: WebsiteBridgeConfig,
    sessionToken: String?,
  ): Result<ParishionerDto> =
    withContext(Dispatchers.IO) {
      if (config.baseUrl.isBlank()) {
        return@withContext Result.failure(IllegalStateException("Website API URL not configured"))
      }
      try {
        val response =
          createService(config.baseUrl)
            .getParishionerProfile(buildAuthHeader(sessionToken, config.apiToken))
        val body = response.body()
        if (response.isSuccessful && body != null) {
          Result.success(body)
        } else {
          Result.failure(IllegalStateException("Profile fetch HTTP ${response.code()}"))
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun fetchCertificateRequests(
    config: WebsiteBridgeConfig,
    sessionToken: String?,
  ): Result<List<CertificateRequestDto>> =
    withContext(Dispatchers.IO) {
      if (config.baseUrl.isBlank()) {
        return@withContext Result.failure(IllegalStateException("Website API URL not configured"))
      }
      try {
        val response =
          createService(config.baseUrl)
            .getCertificateRequests(buildAuthHeader(sessionToken, config.apiToken))
        val body = response.body()
        if (response.isSuccessful && body != null) {
          Result.success(body)
        } else {
          Result.failure(IllegalStateException("Requests fetch HTTP ${response.code()}"))
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun syncProfile(
    config: WebsiteBridgeConfig,
    sessionToken: String?,
    profile: ParishionerProfile,
  ): BridgeSyncOutcome =
    withContext(Dispatchers.IO) {
      if (config.baseUrl.isBlank() || !config.autoSyncEnabled) {
        return@withContext BridgeSyncOutcome.NotConfigured
      }
      try {
        val payload =
          ParishionerSyncPayload(
            userId = profile.userId,
            parishId = profile.parishId,
            fullName = profile.fullName,
            email = profile.email,
            phone = profile.phone,
            address = profile.address,
            sccUnit = profile.sccUnit,
            headOfFamily = profile.headOfFamily,
            dateOfBirth = profile.dateOfBirth,
            baptismDate = profile.baptismDate,
            spouseName = profile.spouseName,
          )
        val response =
          createService(config.baseUrl)
            .upsertParishionerProfile(buildAuthHeader(sessionToken, config.apiToken), payload)
        if (response.isSuccessful) {
          val msg = response.body()?.message ?: "Profile updated in parish website database"
          BridgeSyncOutcome.Synced(msg)
        } else {
          BridgeSyncOutcome.Failed("Website sync HTTP ${response.code()}")
        }
      } catch (e: Exception) {
        BridgeSyncOutcome.Failed(e.localizedMessage ?: "Website profile sync failed")
      }
    }

  suspend fun syncCertificateRequest(
    config: WebsiteBridgeConfig,
    sessionToken: String?,
    request: CertificateRequest,
  ): BridgeSyncOutcome =
    withContext(Dispatchers.IO) {
      if (config.baseUrl.isBlank() || !config.autoSyncEnabled) {
        return@withContext BridgeSyncOutcome.NotConfigured
      }
      try {
        val payload =
          CertificateSyncPayload(
            requestId = request.id,
            userId = request.userId,
            certificateType = request.certificateType,
            applicantName = request.applicantName,
            subjectName = request.subjectName,
            eventDate = request.eventDate,
            placeOrParish = request.placeOrParish,
            parentsOrSpouseNames = request.parentsOrSpouseNames,
            sponsorOrNextOfKin = request.sponsorOrNextOfKin,
            registryReference = request.registryReference,
            purpose = request.purpose,
            notes = request.notes,
            status = request.status,
          )
        val response =
          createService(config.baseUrl)
            .submitCertificateRequest(buildAuthHeader(sessionToken, config.apiToken), payload)
        if (response.isSuccessful) {
          val ref = response.body()?.referenceCode
          val msg =
            if (!ref.isNullOrBlank()) "Synced to parish database (Ref: $ref)"
            else response.body()?.message ?: "Request submitted to parish website API"
          BridgeSyncOutcome.Synced(msg)
        } else {
          BridgeSyncOutcome.Failed("Website sync HTTP ${response.code()}")
        }
      } catch (e: Exception) {
        BridgeSyncOutcome.Failed(e.localizedMessage ?: "Website certificate sync failed")
      }
    }

  suspend fun cancelCertificateRequest(
    config: WebsiteBridgeConfig,
    sessionToken: String?,
    requestId: String,
  ): BridgeSyncOutcome =
    withContext(Dispatchers.IO) {
      if (config.baseUrl.isBlank() || !config.autoSyncEnabled) {
        return@withContext BridgeSyncOutcome.NotConfigured
      }
      try {
        val response =
          createService(config.baseUrl)
            .cancelCertificateRequest(buildAuthHeader(sessionToken, config.apiToken), requestId)
        if (response.isSuccessful) {
          BridgeSyncOutcome.Synced("Cancellation synced with parish website API")
        } else {
          BridgeSyncOutcome.Failed("Cancel sync HTTP ${response.code()}")
        }
      } catch (e: Exception) {
        BridgeSyncOutcome.Failed(e.localizedMessage ?: "Cancel sync failed")
      }
    }
}
