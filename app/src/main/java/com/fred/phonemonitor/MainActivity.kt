package com.fred.phonemonitor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricCollector
import com.fred.phonemonitor.core.MetricUpdateListener
import com.fred.phonemonitor.sources.BatterySource
import com.fred.phonemonitor.sources.CpuSource
import com.fred.phonemonitor.sources.DisplaySource
import com.fred.phonemonitor.sources.MemorySource
import com.fred.phonemonitor.sources.SensorSource
import com.fred.phonemonitor.sources.StorageSource
import com.fred.phonemonitor.sources.SysfsProbeSource
import com.fred.phonemonitor.sources.ThermalStatusSource
import com.fred.phonemonitor.ui.MonitorPagerAdapter
import com.fred.phonemonitor.ui.TabSpec
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var collector: MetricCollector
    private val handler = Handler(Looper.getMainLooper())
    private val listeners = mutableListOf<MetricUpdateListener>()

    private lateinit var switchKeepScreen: SwitchMaterial
    private lateinit var tvStatus: TextView

    /** 最近一次采集结果，供新创建的 Fragment 立即填充 */
    var lastData: Map<String, List<Metric>> = emptyMap()
        private set

    private val ticker = object : Runnable {
        override fun run() {
            lastData = collector.readAll()
            listeners.toList().forEach { it.onMetricsUpdated(lastData) }
            applyKeepScreen()
            handler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        collector = MetricCollector(
            listOf(
                BatterySource(this),
                CpuSource(),
                MemorySource(this),
                StorageSource(),
                DisplaySource(this),
                ThermalStatusSource(this),
                SensorSource(this),
                SysfsProbeSource()
            )
        )

        switchKeepScreen = findViewById(R.id.switchKeepScreen)
        tvStatus = findViewById(R.id.tvStatus)

        setupTabs()

        switchKeepScreen.setOnCheckedChangeListener { _, _ -> applyKeepScreen() }
    }

    private fun setupTabs() {
        val tabs = listOf(
            TabSpec("概览", listOf("battery", "cpu", "memory", "thermal")),
            TabSpec("电池", listOf("battery")),
            TabSpec("系统", listOf("cpu", "memory", "storage", "display")),
            TabSpec("热与传感器", listOf("thermal", "sensors")),
            TabSpec("探测", listOf("probe"))
        )

        val pager = findViewById<ViewPager2>(R.id.pager)
        pager.adapter = MonitorPagerAdapter(this, tabs)

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.text = tabs[position].title
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        handler.post(ticker)
    }

    override fun onPause() {
        handler.removeCallbacks(ticker)
        // 离开前台时主动释放，避免后台耗电
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    fun addListener(listener: MetricUpdateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: MetricUpdateListener) {
        listeners.remove(listener)
    }

    /**
     * 常亮控制：使用 FLAG_KEEP_SCREEN_ON（官方推荐，无需任何权限）。
     * 仅在 app 前台有效——用户锁屏后系统依然会熄屏，这是 Android 的安全设计。
     * 触发条件：用户开关打开 且（正在充电 或 电量不低于阈值）。
     */
    private fun applyKeepScreen() {
        val userEnabled = switchKeepScreen.isChecked
        val batteryOk = batteryOkForKeep()
        val active = userEnabled && batteryOk

        if (active) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        tvStatus.text = when {
            !userEnabled -> "常亮：已关闭"
            !batteryOk -> "常亮：已暂停（电量低于 ${KEEP_SCREEN_MIN_BATTERY}% 且未充电）"
            else -> "常亮：生效中"
        }
    }

    private fun batteryOkForKeep(): Boolean {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return true

        // 只要接了任何外部电源（USB / 交流电 / 无线）就常亮
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        if (plugged != 0) return true

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val percent = if (scale > 0) level * 100 / scale else 100
        return percent >= KEEP_SCREEN_MIN_BATTERY
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 1000L
        private const val KEEP_SCREEN_MIN_BATTERY = 20
    }
}
