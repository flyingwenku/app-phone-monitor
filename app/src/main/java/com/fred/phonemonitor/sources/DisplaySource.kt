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
            Metric("display_refresh", "刷新率", readRefreshRate())
        )
    }

    private fun readRefreshRate(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return "需 Android 6+"
        return try {
            val manager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = manager?.getDisplay(Display.DEFAULT_DISPLAY)
            val rate = display?.refreshRate
            if (rate != null && rate > 0f) String.format(Locale.getDefault(), "%.1f Hz", rate)
            else "—"
        } catch (e: Exception) {
            "—"
        }
    }
}
