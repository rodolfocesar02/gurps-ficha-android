package com.gurps.ficha.vtt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object VttHostAutoDetect {
    private data class ProbeResult(
        val hasApi: Boolean,
        val hasWeb: Boolean
    )

    suspend fun detectLanHostFromArp(timeoutMs: Int = 450): String? = withContext(Dispatchers.IO) {
        val candidates = runCatching { readArpCandidates() }.getOrDefault(emptyList())
        if (candidates.isEmpty()) return@withContext null

        for (host in candidates) {
            val probe = probeHost(host, timeoutMs)
            if (probe.hasApi && probe.hasWeb) return@withContext host
        }

        for (host in candidates) {
            if (probeHost(host, timeoutMs).hasWeb) return@withContext host
        }

        null
    }

    private fun probeHost(host: String, timeoutMs: Int): ProbeResult {
        val apiOk = probeUrl("http://$host:3001/health", timeoutMs)
            || probeUrl("http://$host:3001/", timeoutMs)
        val webOk = probeUrl("http://$host:5176/", timeoutMs)
            || probeUrl("http://$host:5179/", timeoutMs)
        return ProbeResult(hasApi = apiOk, hasWeb = webOk)
    }

    private fun probeUrl(url: String, timeoutMs: Int): Boolean {
        return runCatching {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                instanceFollowRedirects = false
            }
            try {
                val code = conn.responseCode
                code in 200..499
            } finally {
                conn.disconnect()
            }
        }.getOrDefault(false)
    }

    private fun readArpCandidates(): List<String> {
        val arpFile = File("/proc/net/arp")
        if (!arpFile.exists()) return emptyList()

        val out = LinkedHashSet<String>()
        runCatching {
            arpFile.forEachLine { line ->
                val t = line.trim().split(Regex("\\s+"))
                if (t.size < 6) return@forEachLine
                val ip = t[0]
                val flags = t[2]
                val mac = t[3]
                if (flags != "0x2") return@forEachLine
                if (mac.equals("00:00:00:00:00:00", ignoreCase = true)) return@forEachLine
                if (isPrivateIpv4(ip) && ip != "127.0.0.1") out += ip
            }
        }
        return out.toList()
    }

    private fun isPrivateIpv4(ip: String): Boolean {
        val parts = ip.split('.')
        if (parts.size != 4) return false
        val nums = parts.mapNotNull { it.toIntOrNull() }
        if (nums.size != 4) return false
        val a = nums[0]
        val b = nums[1]
        return (a == 10) || (a == 172 && b in 16..31) || (a == 192 && b == 168)
    }
}
