package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.KeystoreRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface KeystoreDao {
    @Query("SELECT * FROM keystores ORDER BY createdAt DESC")
    fun getAllKeystores(): Flow<List<KeystoreRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeystore(keystore: KeystoreRecord): Long

    @Delete
    suspend fun deleteKeystore(keystore: KeystoreRecord)

    @Query("SELECT * FROM keystores WHERE id = :id LIMIT 1")
    suspend fun getKeystoreById(id: Long): KeystoreRecord?
}
