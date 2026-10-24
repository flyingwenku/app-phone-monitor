package com.fred.phonemonitor.sources

import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricSource
import java.io.File
import java.util.Locale

/**
 * CPU 使用率：对 /proc/stat 首行做两次采样求差。
 *
 * 注意：只能拿到使用率，拿不到 CPU 温度（Android 10+ 禁止读 thermal zone）。
 */
class CpuUsageSource : MetricSource {

    override val key = "cpu"
    override val label = "CPU"

    private var prevTotal = 0L
    private var prevIdle = 0L

    override fun read(): List<Metric> {
        val metrics = mutableListOf<Metric>()

        val sample = readStat()
        if (sample == null) {
            metrics.add(Metric("cpu_usage", "CPU 总使用率", "不可用"))
        } else {
            val (total, idle) = sample
            val usage = if (prevTotal > 0L && total > prevTotal) {
                val deltaTotal = (total - prevTotal).toDouble()
                val deltaIdle = (idle - prevIdle).toDouble()
                (1.0 - deltaIdle / deltaTotal) * 100.0
            } else {
                null
            }
            prevTotal = total
            prevIdle = idle

            metrics.add(
                Metric(
                    "cpu_usage", "CPU 总使用率",
                    usage?.let { String.format(Locale.getDefault(), "%.1f%%", it) } ?: "采样中…"
                )
            )
        }

        metrics.add(Metric("cpu_cores", "核心数", Runtime.getRuntime().availableProcessors().toString()))
        return metrics
    }

    /** 返回 (总时间片, 空闲时间片)，失败返回 null。 */
    private fun readStat(): Pair<Long, Long>? {
        // 注意：不能用表达式函数体（= try {...}），否则其中的 return 会编译报错
        val line: String? = try {
            File("/proc/stat").bufferedReader().use { it.readLine() }
        } catch (e: Exception) {
            return null
        }

        if (line.isNullOrEmpty()) return null

        val parts = line.trim().split(Regex("\\s+"))
        if (parts.isEmpty() || !parts[0].startsWith("cpu")) return null

        var total = 0L
        for (i in 1 until parts.size) {
            total += parts[i].toLongOrNull() ?: 0L
        }
        // 索引 4 = idle，索引 5 = iowait
        val idle = (parts.getOrNull(4)?.toLongOrNull() ?: 0L) + (parts.getOrNull(5)?.toLongOrNull() ?: 0L)
        return total to idle
    }
}
