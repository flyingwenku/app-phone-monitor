package com.fred.phonemonitor.sources

import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricSource
import com.fred.phonemonitor.core.formatKhz
import java.io.File
import java.util.Locale

/**
 * CPU 实时状态。
 *
 * 实测结论（荣耀 X50i+ / Android 14）：
 * - /proc/stat 受 SELinux 限制（Android 8+ 所有应用均不可读），总使用率拿不到；
 * - /sys/devices/system/cpu/cpuN/cpufreq/scaling_cur_freq 可读，
 *   频率与负载强相关，作为负载指示足够直观。
 */
class CpuSource : MetricSource {

    override val key = "cpu"
    override val label = "CPU"

    private val coreCount = Runtime.getRuntime().availableProcessors()

    override fun read(): List<Metric> {
        val metrics = mutableListOf<Metric>()

        var sumKhz = 0L
        var readableCores = 0
        for (core in 0 until coreCount) {
            val khz = readKhz("/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq")
            if (khz != null) {
                sumKhz += khz
                readableCores++
                metrics.add(Metric("cpu_freq_$core", "CPU$core 频率", formatKhz(khz)))
            } else {
                metrics.add(Metric("cpu_freq_$core", "CPU$core 频率", "不可读"))
            }
        }

        val avg = if (readableCores > 0) formatKhz(sumKhz / readableCores) else "不可读"
        metrics.add(0, Metric("cpu_avg_freq", "CPU 平均频率", avg))
        metrics.add(Metric("cpu_cores", "核心数", coreCount.toString()))
        metrics.add(Metric("cpu_usage_note", "CPU 使用率", "Android 8+ 系统限制，不可读"))
        return metrics
    }

    private fun readKhz(path: String): Long? = try {
        val file = File(path)
        if (file.canRead()) file.readText().trim().toLongOrNull() else null
    } catch (e: Exception) {
        null
    }
}
