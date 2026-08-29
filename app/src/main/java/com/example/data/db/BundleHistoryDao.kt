package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BundleConversionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BundleHistoryDao {
    @Query("SELECT * FROM bundle_conversions ORDER BY timestamp DESC")
    fun getAllConversions(): Flow<List<BundleConversionRecord>>

    @Query("SELECT * FROM bundle_conversions WHERE id = :id LIMIT 1")
    suspend fun getConversionById(id: Long): BundleConversionRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversion(record: BundleConversionRecord): Long

    @Delete
    suspend fun deleteConversion(record: BundleConversionRecord)

    @Query("DELETE FROM bundle_conversions")
    suspend fun clearAll()
}
