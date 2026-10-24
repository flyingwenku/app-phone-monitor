package com.fred.phonemonitor.sources

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricSource
import java.util.Locale

/** 屏幕参数：分辨率、密度、刷新率。 */
class DisplaySource(private val context: Context) : MetricSource {

    override val key = "display"
    override val label = "屏幕"

    override fun read(): List<Metric> {
        val metrics = context.resources.displayMetrics

        return listOf(
            Metric(
                "display_res", "分辨率",
                "${metrics.widthPixels} × ${metrics.heightPixels}"
            ),
            Metric("display_dpi", "像素密度", "${metrics.densityDpi} dpi"),
            Metric(
                "display_density", "显示缩放",
                String.format(Locale.getDefault(), "%.2f", metrics.density)
            ),
            Metric("display_refresh", "当前刷新率", readCurrentRefreshRate()),
            Metric("display_modes", "支持的刷新率", readSupportedRefreshRates())
        )
    }

    private fun readCurrentRefreshRate(): String {
        val display = defaultDisplay() ?: return "—"
        val rate = display.refreshRate
        return if (rate > 0f) String.format(Locale.getDefault(), "%.1f Hz", rate) else "—"
    }

    /**
     * refreshRate 只报当前模式（60Hz 屏跑 90Hz 模式时会误判），
     * 完整能力要看 supportedModes。
     */
    private fun readSupportedRefreshRates(): String {
        val display = defaultDisplay() ?: return "—"
        return try {
            val rates = display.supportedModes
                .map { it.refreshRate }
                .map { (it * 10).toInt() / 10f }
                .distinct()
                .sorted()
            if (rates.isEmpty()) "—"
            else rates.joinToString(" / ") { String.format(Locale.getDefault(), "%.1f", it) } + " Hz"
        } catch (e: Exception) {
            "—"
        }
    }

    private fun defaultDisplay(): Display? = try {
        (context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
    } catch (e: Exception) {
        null
    }
}
