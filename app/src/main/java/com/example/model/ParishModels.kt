package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CertificateType(
  val label: String,
  val shortLabel: String,
  val subtitle: String,
  val subjectLabel: String,
  val eventDateLabel: String,
  val parentsLabel: String,
  val sponsorLabel: String,
  val placeLabel: String,
) {
  BIRTH(
    label = "Birth Certificate",
    shortLabel = "Birth",
    subtitle = "Parish birth & baptismal extract",
    subjectLabel = "Child / Applicant Full Name",
    eventDateLabel = "Date of Birth (YYYY-MM-DD)",
    parentsLabel = "Father & Mother Full Names",
    sponsorLabel = "Godparents / Informant",
    placeLabel = "Hospital or Place of Birth",
  ),
  COMMUNION(
    label = "Communion Certificate",
    shortLabel = "Communion",
    subtitle = "First Holy Communion sacrament record",
    subjectLabel = "Communicant Full Name",
    eventDateLabel = "Date of First Communion (YYYY-MM-DD)",
    parentsLabel = "Father & Mother Full Names",
    sponsorLabel = "Parish Priest / Catechist (Optional)",
    placeLabel = "Church / Chapel Name",
  ),
  CONFIRMATION(
    label = "Confirmation Certificate",
    shortLabel = "Confirmation",
    subtitle = "Sacrament of Confirmation record",
    subjectLabel = "Confirmand Full Name",
    eventDateLabel = "Date of Confirmation (YYYY-MM-DD)",
    parentsLabel = "Father & Mother Full Names",
    sponsorLabel = "Confirmation Sponsor Name",
    placeLabel = "Confirmation Name & Parish",
  ),
  MARRIAGE(
    label = "Marriage Certificate",
    shortLabel = "Marriage",
    subtitle = "Holy Matrimony parish register extract",
    subjectLabel = "Primary Applicant / Couple Title",
    eventDateLabel = "Date of Marriage (YYYY-MM-DD)",
    parentsLabel = "Groom & Bride Full Names",
    sponsorLabel = "Marriage Witnesses Names",
    placeLabel = "Church / Solemnization Parish",
  ),
  DEATH(
    label = "Death Certificate",
    shortLabel = "Death",
    subtitle = "Parish burial & obituary register extract",
    subjectLabel = "Deceased Full Name",
    eventDateLabel = "Date of Passing (YYYY-MM-DD)",
    parentsLabel = "Spouse or Parents Name",
    sponsorLabel = "Next of Kin & Relationship",
    placeLabel = "Burial Date & Cemetery",
  );

  companion object {
    fun fromName(name: String): CertificateType =
      entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: BIRTH
  }
}

enum class RequestStatus(val label: String) {
  SUBMITTED("Submitted"),
  UNDER_REVIEW("Under Review"),
  READY("Ready for Collection"),
  CANCELLED("Cancelled");

  companion object {
    fun fromName(name: String): RequestStatus =
      entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SUBMITTED
  }
}

enum class WebsiteSyncStatus(val label: String) {
  SYNCED_TO_WEBSITE("Synced with Parish API"),
  PENDING_API_SYNC("Pending API Sync"),
  DUMMY_TEST_MODE("Dummy Test Mode");

  companion object {
    fun fromName(name: String): WebsiteSyncStatus =
      entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PENDING_API_SYNC
  }
}

@Entity(tableName = "parishioner_profiles")
data class ParishionerProfile(
  @PrimaryKey val userId: String,
  val parishId: String = "",
  val fullName: String = "",
  val email: String = "",
  val phone: String = "",
  val address: String = "",
  val sccUnit: String = "",
  val headOfFamily: String = "",
  val dateOfBirth: String = "",
  val baptismDate: String = "",
  val spouseName: String = "",
  val websiteSyncStatus: String = WebsiteSyncStatus.PENDING_API_SYNC.name,
  val createdAtMillis: Long = System.currentTimeMillis(),
  val updatedAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "certificate_requests")
data class CertificateRequest(
  @PrimaryKey val id: String,
  val userId: String,
  val certificateType: String = CertificateType.BIRTH.name,
  val applicantName: String = "",
  val subjectName: String = "",
  val eventDate: String = "",
  val placeOrParish: String = "",
  val parentsOrSpouseNames: String = "",
  val sponsorOrNextOfKin: String = "",
  val registryReference: String = "",
  val purpose: String = "",
  val notes: String = "",
  val status: String = RequestStatus.SUBMITTED.name,
  val websiteSyncStatus: String = WebsiteSyncStatus.PENDING_API_SYNC.name,
  val createdAtMillis: Long = System.currentTimeMillis(),
  val updatedAtMillis: Long = System.currentTimeMillis(),
)
