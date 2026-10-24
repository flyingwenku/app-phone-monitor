package com.fred.phonemonitor.core

import java.util.Locale

/** 字节数格式化为 GB / MB。 */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "—"
    val gb = bytes / 1024.0 / 1024.0 / 1024.0
    return if (gb >= 1.0) {
        String.format(Locale.getDefault(), "%.2f GB", gb)
    } else {
        String.format(Locale.getDefault(), "%.0f MB", bytes / 1024.0 / 1024.0)
    }
}

/** 频率（kHz）格式化为 MHz / GHz。 */
fun formatKhz(khz: Long): String {
    if (khz <= 0L) return "—"
    val mhz = khz / 1000.0
    return if (mhz >= 1000.0) {
        String.format(Locale.getDefault(), "%.2f GHz", mhz / 1000.0)
    } else {
        String.format(Locale.getDefault(), "%.0f MHz", mhz)
    }
}
