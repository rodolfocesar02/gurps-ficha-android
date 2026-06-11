package com.gurps.ficha.vtt

import android.os.Build
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.HttpURLConnection
import java.net.URL

object VttHostAutoDetect {
    private data class ProbeResult(
        val hasApi: Boolean,
        val hasWeb: Boolean
    )

    suspend fun detectLanHost(timeoutMs: Int = 450): String? = withContext(Dispatchers.IO) {
        detectLanHostFromArp(timeoutMs)?.let { return@withContext it }
        detectLanHostByActiveScan(timeoutMs)
    }

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

    suspend fun detectLanHostByActiveScan(timeoutMs: Int = 450): String? = withContext(Dispatchers.IO) {
        val basePrefixes = localPrivatePrefixes()
        if (basePrefixes.isEmpty()) return@withContext null

        val localIps = localPrivateIps().toSet()
        val candidates = mutableListOf<String>()
        for (prefix in basePrefixes) {
            for (h in 1..254) {
                val ip = "$prefix.$h"
                if (ip !in localIps) candidates += ip
            }
        }

        if (candidates.isEmpty()) return@withContext null

        // Fase 1: busca host que responde API+WEB.
        val both = firstHostMatching(candidates, timeoutMs) { it.hasApi && it.hasWeb }
        if (both != null) return@withContext both

        // Fase 2: aceita host com WEB disponível (útil quando API está em outra porta/host temporário).
        val webOnly = firstHostMatching(candidates, timeoutMs) { it.hasWeb }
        if (webOnly != null) return@withContext webOnly

        null
    }

    private suspend fun firstHostMatching(
        candidates: List<String>,
        timeoutMs: Int,
        predicate: (ProbeResult) -> Boolean
    ): String? = coroutineScope {
        val chunkSize = 20
        for (chunk in candidates.chunked(chunkSize)) {
            val results = chunk.map { host ->
                async(Dispatchers.IO) {
                    host to probeHost(host, timeoutMs)
                }
            }.awaitAll()
            val hit = results.firstOrNull { (_, probe) -> predicate(probe) }?.first
            if (hit != null) return@coroutineScope hit
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
        // Android 10+ (API 29) bloqueia a leitura de /proc/net/arp (retorna vazio ou
        // SecurityException). Nesses aparelhos pulamos direto para o scan ativo.
        if (Build.VERSION.SDK_INT >= 29) return emptyList()
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

    private fun localPrivateIps(): List<String> {
        return runCatching {
            val out = mutableListOf<String>()
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            for (netIf in interfaces) {
                if (!netIf.isUp || netIf.isLoopback || netIf.isVirtual) continue
                val addresses = netIf.inetAddresses?.toList().orEmpty()
                for (addr in addresses) {
                    if (addr is Inet4Address) {
                        val ip = addr.hostAddress ?: continue
                        if (isPrivateIpv4(ip)) out += ip
                    }
                }
            }
            out.distinct()
        }.getOrDefault(emptyList())
    }

    private fun localPrivatePrefixes(): List<String> {
        return localPrivateIps()
            .mapNotNull { ip ->
                val parts = ip.split('.')
                if (parts.size == 4) "${parts[0]}.${parts[1]}.${parts[2]}" else null
            }
            .distinct()
    }
}
