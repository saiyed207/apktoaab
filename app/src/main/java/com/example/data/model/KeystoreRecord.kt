package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keystores")
data class KeystoreRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alias: String,
    val filePath: String,
    val keyAlgorithm: String = "RSA 2048",
    val validityYears: Int = 25,
    val certificateOwner: String = "CN=Developer",
    val sha256Fingerprint: String = "",
    val sha1Fingerprint: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
