package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.CertificateRequest
import com.example.model.ParishionerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ParishDao {
  @Query("SELECT * FROM parishioner_profiles WHERE userId = :userId LIMIT 1")
  fun observeProfile(userId: String): Flow<ParishionerProfile?>

  @Query("SELECT * FROM parishioner_profiles WHERE userId = :userId LIMIT 1")
  suspend fun getProfile(userId: String): ParishionerProfile?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertProfile(profile: ParishionerProfile)

  @Query(
    "SELECT * FROM certificate_requests WHERE userId = :userId ORDER BY createdAtMillis DESC"
  )
  fun observeCertificateRequests(userId: String): Flow<List<CertificateRequest>>

  @Query(
    "SELECT * FROM certificate_requests WHERE userId = :userId ORDER BY createdAtMillis DESC"
  )
  suspend fun getCertificateRequests(userId: String): List<CertificateRequest>

  @Query("SELECT * FROM certificate_requests WHERE id = :requestId LIMIT 1")
  suspend fun getCertificateRequestById(requestId: String): CertificateRequest?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertCertificateRequest(request: CertificateRequest)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertCertificateRequests(requests: List<CertificateRequest>)

  @Query(
    "UPDATE certificate_requests SET status = :status, updatedAtMillis = :updatedAt WHERE id = :requestId AND userId = :userId"
  )
  suspend fun updateRequestStatus(
    userId: String,
    requestId: String,
    status: String,
    updatedAt: Long,
  )

  @Query(
    "UPDATE certificate_requests SET websiteSyncStatus = :syncStatus, updatedAtMillis = :updatedAt WHERE id = :requestId AND userId = :userId"
  )
  suspend fun updateRequestSyncStatus(
    userId: String,
    requestId: String,
    syncStatus: String,
    updatedAt: Long,
  )
}

@Database(
  entities = [ParishionerProfile::class, CertificateRequest::class],
  version = 1,
  exportSchema = false,
)
abstract class ParishDatabase : RoomDatabase() {
  abstract fun parishDao(): ParishDao

  companion object {
    @Volatile private var instance: ParishDatabase? = null

    fun getInstance(context: Context): ParishDatabase =
      instance
        ?: synchronized(this) {
          instance
            ?: Room.databaseBuilder(
                context.applicationContext,
                ParishDatabase::class.java,
                "st_anthonys_parish.db",
              )
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
              .also { instance = it }
        }
  }
}
