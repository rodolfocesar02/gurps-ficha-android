package com.gurps.ficha.update

import com.google.gson.Gson
import com.gurps.ficha.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateMetadata(
    val versionCode: Int,
    val versionName: String,
    val visualApkUrl: String? = null,
    val pracegoApkUrl: String? = null,
    val notes: String? = null
)

data class AppUpdateState(
    val hasUpdate: Boolean,
    val currentVersionCode: Int,
    val currentVersionName: String,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val apkUrl: String?,
    val notes: String?
)

object AppUpdateService {
    private val gson = Gson()

    suspend fun checkForUpdates(metadataUrl: String = BuildConfig.UPDATE_METADATA_URL): Result<AppUpdateState> {
        if (metadataUrl.isBlank()) {
            return Result.failure(IllegalStateException("URL de atualização não configurada."))
        }
        return runCatching {
            val payload = downloadText(metadataUrl)
            val metadata = gson.fromJson(payload, AppUpdateMetadata::class.java)
            val chosenApkUrl = chooseApkUrl(metadata)
            AppUpdateState(
                hasUpdate = metadata.versionCode > BuildConfig.VERSION_CODE,
                currentVersionCode = BuildConfig.VERSION_CODE,
                currentVersionName = BuildConfig.VERSION_NAME,
                latestVersionCode = metadata.versionCode,
                latestVersionName = metadata.versionName,
                apkUrl = chosenApkUrl,
                notes = metadata.notes
            )
        }
    }

    private suspend fun downloadText(url: String): String = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) error("Falha HTTP ao consultar atualização: $code")
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun chooseApkUrl(metadata: AppUpdateMetadata): String? {
        return if (BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)) {
            metadata.pracegoApkUrl ?: metadata.visualApkUrl
        } else {
            metadata.visualApkUrl ?: metadata.pracegoApkUrl
        }
    }
}
