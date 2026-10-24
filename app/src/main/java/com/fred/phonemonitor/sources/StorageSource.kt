package com.fred.phonemonitor.sources

import android.os.Environment
import android.os.StatFs
import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricSource
import com.fred.phonemonitor.core.formatBytes

/** 内部存储：StatFs。 */
class StorageSource : MetricSource {

    override val key = "storage"
    override val label = "存储"

    override fun read(): List<Metric> = try {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes
        val available = stat.availableBytes
        val used = (total - available).coerceAtLeast(0L)

        val percent = if (total > 0L) used * 100 / total else 0L

        listOf(
            Metric("storage_total", "内部存储总量", formatBytes(total)),
            Metric("storage_used", "已用", formatBytes(used)),
            Metric("storage_available", "可用", formatBytes(available)),
            Metric("storage_percent", "使用率", "$percent%")
        )
    } catch (e: Exception) {
        listOf(Metric("storage_na", "存储", "不可用"))
    }
}
