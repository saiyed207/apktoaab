package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bundle_conversions")
data class BundleConversionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val packageName: String,
    val appLabel: String,
    val versionName: String,
    val versionCode: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val apkSizeBytes: Long,
    val aabSizeBytes: Long,
    val aabFilePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSigned: Boolean = false,
    val signAlias: String? = null,
    val complianceScore: Int = 100,
    val abiList: String = "",
    val dexCount: Int = 1,
    val permissionCount: Int = 0,
    val status: String = "SUCCESS"
)
