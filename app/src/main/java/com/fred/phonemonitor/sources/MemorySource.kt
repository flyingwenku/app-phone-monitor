package com.fred.phonemonitor.sources

import android.app.ActivityManager
import android.content.Context
import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricSource
import com.fred.phonemonitor.core.formatBytes

/** 内存：使用公开 API ActivityManager.MemoryInfo。 */
class MemorySource(private val context: Context) : MetricSource {

    override val key = "memory"
    override val label = "内存"

    override fun read(): List<Metric> {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return listOf(Metric("memory_na", "内存", "不可用"))

        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)

        return listOf(
            Metric("mem_total", "总内存", formatBytes(info.totalMem)),
            Metric("mem_avail", "可用内存", formatBytes(info.availMem)),
            Metric("mem_used", "已用内存", formatBytes(info.totalMem - info.availMem)),
            Metric("mem_threshold", "低内存阈值", formatBytes(info.threshold)),
            Metric("mem_low", "处于低内存状态", if (info.lowMemory) "是" else "否")
        )
    }
}
