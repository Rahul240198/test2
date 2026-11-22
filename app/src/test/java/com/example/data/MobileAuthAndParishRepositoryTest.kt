package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.bridge.WebsiteApiBridgeClient
import com.example.data.bridge.WebsiteBridgePreferences
import com.example.data.local.ParishDatabase
import com.example.model.CertificateRequest
import com.example.model.CertificateType
import com.example.model.RequestStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MobileAuthAndParishRepositoryTest {

  private lateinit var db: ParishDatabase
  private lateinit var repo: ParishRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, ParishDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val prefs = WebsiteBridgePreferences(context)
    repo = ParishRepository(db.parishDao(), prefs, WebsiteApiBridgeClient())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun mobileNormalization_handlesCountryCodeAndLeadingZero() {
    assertEquals("9823012345", ParishRepository.normalizeMobileNumber("+91 98230 12345"))
    assertEquals("9823012345", ParishRepository.normalizeMobileNumber("09823012345"))
    assertEquals("9823012345", ParishRepository.normalizeMobileNumber("9823012345"))
  }

  @Test
  fun dummyLogin_seedsProfileAndSupportsProfileUpdateAndAllFiveCertificates() = runBlocking {
    val loginResult = repo.loginWithDummyAccount("9823012345", null)
    assertTrue(loginResult.isSuccess)
    val session = loginResult.getOrThrow()
    assertTrue(session.isLoggedIn)
    assertTrue(session.isDummyMode)
    assertEquals("+91 9823012345", session.mobileNumber)

    val profile = repo.getProfile(session.userId)
    assertNotNull(profile)
    assertEquals("Maria D'Souza", profile?.fullName)

    repo.saveProfile(profile!!.copy(sccUnit = "Holy Family Ward"))
    val updated = repo.getProfile(session.userId)
    assertEquals("Holy Family Ward", updated?.sccUnit)

    val submittedIds =
      CertificateType.entries.map { type ->
        repo.submitCertificateRequest(
          CertificateRequest(
            id = "",
            userId = session.userId,
            certificateType = type.name,
            applicantName = updated!!.fullName,
            subjectName = "Test Subject ${type.shortLabel}",
            eventDate = "2024-05-01",
            purpose = "Verification",
          )
        ).id
      }

    val requests = repo.observeCertificateRequests(session.userId).first()
    assertTrue(submittedIds.all { id -> requests.any { it.id == id } })

    repo.cancelCertificateRequest(session.userId, submittedIds.first())
    val afterCancel = repo.observeCertificateRequests(session.userId).first()
    assertEquals(
      RequestStatus.CANCELLED.name,
      afterCancel.first { it.id == submittedIds.first() }.status,
    )

    repo.signOut()
    assertFalse(repo.sessionFlow.first().isLoggedIn)
  }

  @Test
  fun liveMobileLogin_withoutConfiguredUrl_returnsHelpfulError() = runBlocking {
    val result = repo.loginWithMobileApi("9823012345", null)
    assertTrue(result.isFailure)
  }
}
