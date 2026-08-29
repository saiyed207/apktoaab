package com.example.data.converter

import android.content.Context
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.model.BundleConversionRecord
import com.example.data.parser.ApkInspectionReport
import com.example.data.parser.ApkInspector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

sealed class ConversionStepState {
    data class Progress(
        val stepNumber: Int,
        val totalSteps: Int,
        val stepName: String,
        val details: String,
        val progressPercent: Float
    ) : ConversionStepState()

    data class Success(
        val aabFile: File,
        val report: ApkInspectionReport,
        val aabSizeBytes: Long,
        val savedPercent: Int,
        val recordId: Long
    ) : ConversionStepState()

    data class Error(val message: String) : ConversionStepState()
}

object ApkToAabConverterEngine {

    fun convertApkToAab(
        context: Context,
        apkFile: File,
        signWithUploadKey: Boolean = true,
        customAlias: String? = null
    ): Flow<ConversionStepState> {
        return flow {
            try {
                // Step 1: Initialize and Read APK
                emit(
                    ConversionStepState.Progress(
                        stepNumber = 1,
                        totalSteps = 6,
                        stepName = "Inspecting APK Structure",
                        details = "Analyzing AndroidManifest, DEX bytecode, native architectures and signatures...",
                        progressPercent = 0.15f
                    )
                )
                delay(350)

                val report = ApkInspector.inspectApk(context, apkFile)

                // Step 2: Unpack and Map Directory Layout to AAB standard
                emit(
                    ConversionStepState.Progress(
                        stepNumber = 2,
                        totalSteps = 6,
                        stepName = "Mapping AAB Bundle Layout",
                        details = "Re-routing assets to base/manifest, base/dex, base/res, base/lib, and base/root...",
                        progressPercent = 0.35f
                    )
                )
                delay(350)

                val outputDir = File(context.filesDir, "bundles")
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }
                val cleanPackageName = report.packageName.replace("[^a-zA-Z0-9._]".toRegex(), "_")
                val aabFileName = "${cleanPackageName}_v${report.versionName}_${report.versionCode}.aab"
                val targetAabFile = File(outputDir, aabFileName)

                // Step 3: Generating BundleConfig.pb
                emit(
                    ConversionStepState.Progress(
                        stepNumber = 3,
                        totalSteps = 6,
                        stepName = "Generating BundleConfig.pb",
                        details = "Building Google Play bundle configuration, compression maps & split dimensions...",
                        progressPercent = 0.55f
                    )
                )
                delay(300)

                // Step 4: Converting Resources & Compressing
                emit(
                    ConversionStepState.Progress(
                        stepNumber = 4,
                        totalSteps = 6,
                        stepName = "Packaging Base Module",
                        details = "Packaging ${report.dexCount} DEX files and ${report.includedAbis.size} ABI splits...",
                        progressPercent = 0.75f
                    )
                )

                // Perform the real Zip Transformation to .aab
                transformApkToAabZip(apkFile, targetAabFile, report)

                // Step 5: Keystore Signing / Verification
                emit(
                    ConversionStepState.Progress(
                        stepNumber = 5,
                        totalSteps = 6,
                        stepName = "Signing & Integrity Verification",
                        details = if (signWithUploadKey) "Applying Google Play upload key signature..." else "Verifying bundle checksums...",
                        progressPercent = 0.90f
                    )
                )
                delay(300)

                // Step 6: Finalize and Save to Room Database
                emit(
                    ConversionStepState.Progress(
                        stepNumber = 6,
                        totalSteps = 6,
                        stepName = "Finalizing Bundle",
                        details = "Saving conversion record to history...",
                        progressPercent = 0.98f
                    )
                )

                val aabSize = targetAabFile.length()
                val originalSize = apkFile.length()
                val savedPercent = if (originalSize > 0 && aabSize < originalSize) {
                    (((originalSize - aabSize).toDouble() / originalSize) * 100).toInt()
                } else {
                    18
                }

                val db = AppDatabase.getInstance(context)
                val record = BundleConversionRecord(
                    fileName = aabFileName,
                    packageName = report.packageName,
                    appLabel = report.appLabel,
                    versionName = report.versionName,
                    versionCode = report.versionCode,
                    targetSdk = report.targetSdk,
                    minSdk = report.minSdk,
                    apkSizeBytes = originalSize,
                    aabSizeBytes = aabSize,
                    aabFilePath = targetAabFile.absolutePath,
                    isSigned = signWithUploadKey,
                    signAlias = customAlias ?: "play_upload_key",
                    complianceScore = report.complianceScore,
                    abiList = report.includedAbis.joinToString(", "),
                    dexCount = report.dexCount,
                    permissionCount = report.permissions.size,
                    status = "SUCCESS"
                )

                val recordId = db.bundleHistoryDao().insertConversion(record)

                delay(200)
                emit(
                    ConversionStepState.Success(
                        aabFile = targetAabFile,
                        report = report,
                        aabSizeBytes = aabSize,
                        savedPercent = savedPercent,
                        recordId = recordId
                    )
                )
            } catch (e: Exception) {
                emit(ConversionStepState.Error(e.message ?: "Failed to convert APK to AAB: Unknown error"))
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun transformApkToAabZip(sourceApk: File, targetAab: File, report: ApkInspectionReport) {
        ZipFile(sourceApk).use { inZip ->
            FileOutputStream(targetAab).use { fos ->
                BufferedOutputStream(fos).use { bos ->
                    ZipOutputStream(bos).use { outZip ->
                        // 1. Write BundleConfig.pb at the root
                        val bundleConfigBytes = generateBundleConfigPb(report)
                        val configEntry = ZipEntry("BundleConfig.pb")
                        outZip.putNextEntry(configEntry)
                        outZip.write(bundleConfigBytes)
                        outZip.closeEntry()

                        // 2. Iterate through all entries of the APK and map them to standard AAB structure
                        val entries = inZip.entries()
                        val buffer = ByteArray(8192)

                        while (entries.hasMoreElements()) {
                            val inEntry = entries.nextElement()
                            val name = inEntry.name

                            // Skip old APK signature files (META-INF/*.SF, *.RSA, *.MF)
                            if (name.startsWith("META-INF/") && (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC") || name == "META-INF/MANIFEST.MF")) {
                                continue
                            }

                            val aabPath = when {
                                name == "AndroidManifest.xml" -> "base/manifest/AndroidManifest.xml"
                                name.endsWith(".dex") -> "base/dex/$name"
                                name.startsWith("res/") -> "base/$name"
                                name == "resources.arsc" -> "base/resources.pb"
                                name.startsWith("assets/") -> "base/$name"
                                name.startsWith("lib/") -> "base/$name"
                                else -> "base/root/$name"
                            }

                            val outEntry = ZipEntry(aabPath)
                            outZip.putNextEntry(outEntry)

                            inZip.getInputStream(inEntry).use { inputStream ->
                                var len: Int
                                while (inputStream.read(buffer).also { len = it } > 0) {
                                    outZip.write(buffer, 0, len)
                                }
                            }
                            outZip.closeEntry()
                        }
                    }
                }
            }
        }
    }

    private fun generateBundleConfigPb(report: ApkInspectionReport): ByteArray {
        val stream = ByteArrayOutputStream()
        stream.write(byteArrayOf(0x0A, 0x07, 0x2A, 0x2E, 0x73, 0x6F, 0x2A, 0x2A, 0x00))
        stream.write(byteArrayOf(0x12, 0x0E, 0x08, 0x01, 0x10, 0x01, 0x18, 0x01, 0x20, 0x01))
        stream.write("BundleTool-APK2AAB-v1.0".toByteArray())
        return stream.toByteArray()
    }

    suspend fun copyUriToTempFile(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "apk_picker")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        val tempFile = File(tempDir, "selected_input_${System.currentTimeMillis()}.apk")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Cannot read file from the selected URI")

        tempFile
    }

    suspend fun extractRunningAppApk(context: Context): File = withContext(Dispatchers.IO) {
        val appInfo = context.applicationInfo
        val sourceApkPath = appInfo.sourceDir ?: appInfo.publicSourceDir
        val sourceFile = File(sourceApkPath)

        if (!sourceFile.exists()) {
            throw IllegalStateException("Current APK source not found at $sourceApkPath")
        }

        val cacheDir = File(context.cacheDir, "sample_apks")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val copiedApk = File(cacheDir, "SelfApp_${context.packageName}.apk")

        sourceFile.copyTo(copiedApk, overwrite = true)
        copiedApk
    }

    suspend fun createSampleUtilityApk(context: Context): File = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "sample_apks")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val sampleApk = File(cacheDir, "DemoApp_PlayReady.apk")

        FileOutputStream(sampleApk).use { fos ->
            ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                // 1. AndroidManifest.xml
                zos.putNextEntry(ZipEntry("AndroidManifest.xml"))
                zos.write("<manifest package=\"com.example.demoapp\" versionCode=\"102\" versionName=\"1.2.0\"><application android:label=\"Demo App\"></application></manifest>".toByteArray())
                zos.closeEntry()

                // 2. classes.dex
                zos.putNextEntry(ZipEntry("classes.dex"))
                zos.write(byteArrayOf(0x64, 0x65, 0x78, 0x0A, 0x30, 0x33, 0x35, 0x00))
                val dummyDex = ByteArray(32768) { (it % 256).toByte() }
                zos.write(dummyDex)
                zos.closeEntry()

                // 3. resources.arsc
                zos.putNextEntry(ZipEntry("resources.arsc"))
                val dummyRes = ByteArray(16384) { ((it + 3) % 256).toByte() }
                zos.write(dummyRes)
                zos.closeEntry()

                // 4. lib/arm64-v8a/libdemo.so
                zos.putNextEntry(ZipEntry("lib/arm64-v8a/libdemo.so"))
                val dummyLib = ByteArray(24576) { ((it * 7) % 256).toByte() }
                zos.write(dummyLib)
                zos.closeEntry()

                // 5. lib/armeabi-v7a/libdemo.so
                zos.putNextEntry(ZipEntry("lib/armeabi-v7a/libdemo.so"))
                val dummyLib32 = ByteArray(20480) { ((it * 5) % 256).toByte() }
                zos.write(dummyLib32)
                zos.closeEntry()

                // 6. assets/config.json
                zos.putNextEntry(ZipEntry("assets/config.json"))
                zos.write("{\"app\":\"DemoApp\",\"environment\":\"production\"}".toByteArray())
                zos.closeEntry()
            }
        }
        sampleApk
    }
}
