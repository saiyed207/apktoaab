package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.converter.ApkToAabConverterEngine
import com.example.data.converter.ConversionStepState
import com.example.data.db.AppDatabase
import com.example.data.model.BundleConversionRecord
import com.example.data.model.KeystoreRecord
import com.example.data.parser.ApkInspectionReport
import com.example.data.parser.ApkInspector
import com.example.data.security.KeystoreGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    // Current APK & Inspection
    private val _selectedApkFile = MutableStateFlow<File?>(null)
    val selectedApkFile: StateFlow<File?> = _selectedApkFile.asStateFlow()

    private val _inspectionReport = MutableStateFlow<ApkInspectionReport?>(null)
    val inspectionReport: StateFlow<ApkInspectionReport?> = _inspectionReport.asStateFlow()

    private val _isInspecting = MutableStateFlow(false)
    val isInspecting: StateFlow<Boolean> = _isInspecting.asStateFlow()

    // Conversion State
    private val _conversionState = MutableStateFlow<ConversionStepState?>(null)
    val conversionState: StateFlow<ConversionStepState?> = _conversionState.asStateFlow()

    // Settings for conversion
    val signWithUploadKey = MutableStateFlow(true)
    val customKeyAlias = MutableStateFlow("play_upload_key")

    // Database Flows
    val conversionHistory: StateFlow<List<BundleConversionRecord>> = db.bundleHistoryDao()
        .getAllConversions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val keystores: StateFlow<List<KeystoreRecord>> = db.keystoreDao()
        .getAllKeystores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Auto-load demo sample on initial launch so the user immediately sees a live inspected APK
        viewModelScope.launch {
            loadSampleDemoApk(application)
        }
    }

    fun loadApkFile(context: Context, file: File) {
        viewModelScope.launch {
            _isInspecting.value = true
            _selectedApkFile.value = file
            _conversionState.value = null
            try {
                val report = withContext(Dispatchers.IO) {
                    ApkInspector.inspectApk(context, file)
                }
                _inspectionReport.value = report
            } catch (e: Exception) {
                Toast.makeText(context, "Error inspecting APK: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isInspecting.value = false
            }
        }
    }

    fun loadApkFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isInspecting.value = true
            try {
                val tempFile = ApkToAabConverterEngine.copyUriToTempFile(context, uri)
                loadApkFile(context, tempFile)
            } catch (e: Exception) {
                _isInspecting.value = false
                Toast.makeText(context, "Failed to load APK: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun loadRunningAppApk(context: Context) {
        viewModelScope.launch {
            _isInspecting.value = true
            try {
                val extracted = ApkToAabConverterEngine.extractRunningAppApk(context)
                loadApkFile(context, extracted)
                Toast.makeText(context, "Loaded installed app APK!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                _isInspecting.value = false
                Toast.makeText(context, "Failed to extract APK: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadSampleDemoApk(context: Context) {
        viewModelScope.launch {
            _isInspecting.value = true
            try {
                val sample = ApkToAabConverterEngine.createSampleUtilityApk(context)
                loadApkFile(context, sample)
            } catch (e: Exception) {
                _isInspecting.value = false
            }
        }
    }

    fun startConversion(context: Context) {
        val file = _selectedApkFile.value ?: return
        viewModelScope.launch {
            ApkToAabConverterEngine.convertApkToAab(
                context = context,
                apkFile = file,
                signWithUploadKey = signWithUploadKey.value,
                customAlias = customKeyAlias.value.ifBlank { "play_upload_key" }
            ).collect { step ->
                _conversionState.value = step
            }
        }
    }

    fun resetConversion() {
        _conversionState.value = null
    }

    fun shareAabFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Google Play Android App Bundle (.aab)")
                putExtra(Intent.EXTRA_TEXT, "Here is the converted Android App Bundle (.aab) ready for Google Play Console release: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share AAB Bundle"))
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareAabFromRecord(context: Context, record: BundleConversionRecord) {
        val file = File(record.aabFilePath)
        if (file.exists()) {
            shareAabFile(context, file)
        } else {
            Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    fun createKeystore(
        context: Context,
        alias: String,
        password: String,
        owner: String,
        org: String,
        validityYears: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val info = withContext(Dispatchers.IO) {
                    KeystoreGenerator.generateKeystore(
                        context = context,
                        alias = alias,
                        password = password,
                        commonName = owner,
                        organization = org,
                        validityYears = validityYears
                    )
                }

                val record = KeystoreRecord(
                    alias = info.alias,
                    filePath = info.file.absolutePath,
                    keyAlgorithm = "RSA 2048",
                    validityYears = info.validityYears,
                    certificateOwner = info.owner,
                    sha256Fingerprint = info.sha256Fingerprint,
                    sha1Fingerprint = info.sha1Fingerprint
                )

                db.keystoreDao().insertKeystore(record)
                Toast.makeText(context, "Keystore created & saved for Play App Signing!", Toast.LENGTH_SHORT).show()
                onSuccess()
            } catch (e: Exception) {
                Toast.makeText(context, "Error generating keystore: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteConversion(record: BundleConversionRecord) {
        viewModelScope.launch {
            try {
                val file = File(record.aabFilePath)
                if (file.exists()) file.delete()
                db.bundleHistoryDao().deleteConversion(record)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            db.bundleHistoryDao().clearAll()
        }
    }

    fun deleteKeystore(keystore: KeystoreRecord) {
        viewModelScope.launch {
            try {
                val file = File(keystore.filePath)
                if (file.exists()) file.delete()
                db.keystoreDao().deleteKeystore(keystore)
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
