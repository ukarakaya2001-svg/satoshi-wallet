package com.example.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey val id: String,
  val txHash: String,
  val network: String,
  val type: String,
  val amountSatoshis: Long,
  val feeSatoshis: Long,
  val recipientOrSender: String,
  val timestamp: Long,
  val memo: String,
  val status: String,
  val assetSymbol: String = "BTC",
  val customAmountText: String? = null,
  val customFeeText: String? = null
)

@Entity(tableName = "cloud_backups")
data class CloudBackupEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val timestamp: Long,
  val cipherAlgorithm: String,
  val kdfIterations: Int,
  val sha256Checksum: String,
  val payloadSize: Int,
  val isSyncedToCloud: Boolean
)

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(transactions: List<TransactionEntity>)

  @Query("DELETE FROM transactions")
  suspend fun clearAll()
}

@Dao
interface CloudBackupDao {
  @Query("SELECT * FROM cloud_backups ORDER BY timestamp DESC")
  fun getAllBackups(): Flow<List<CloudBackupEntity>>

  @Query("SELECT * FROM cloud_backups ORDER BY timestamp DESC LIMIT 1")
  suspend fun getLatestBackup(): CloudBackupEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBackup(backup: CloudBackupEntity)
}

@Database(
  entities = [TransactionEntity::class, CloudBackupEntity::class],
  version = 1,
  exportSchema = false
)
abstract class SatoshiWalletDatabase : RoomDatabase() {
  abstract fun transactionDao(): TransactionDao
  abstract fun cloudBackupDao(): CloudBackupDao
}
