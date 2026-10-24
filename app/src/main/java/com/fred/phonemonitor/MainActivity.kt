package com.fred.phonemonitor

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * 阶段 0 + 1：验证流水线可出包，并采集电池核心数据。
 *
 * 说明：电池温度是本应用唯一能稳定拿到「具体摄氏度」的温度指标。
 * CPU 温度在 Android 10+ 上受 SELinux 限制无法读取，后续由
 * ThermalStatusSource（热状态等级）+ SysfsProbeSource（实证探测）覆盖。
 */
class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private lateinit var tvTemp: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvCurrent: TextView
    private lateinit var tvHealth: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTech: TextView
    private lateinit var tvDevice: TextView
    private lateinit var tvUpdated: TextView

    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        showDeviceInfo()
    }

    override fun onResume() {
        super.onResume()
        handler.post(ticker)
    }

    override fun onPause() {
        handler.removeCallbacks(ticker)
        super.onPause()
    }

    private fun bindViews() {
        tvTemp = findViewById(R.id.tvTemp)
        tvLevel = findViewById(R.id.tvLevel)
        tvVoltage = findViewById(R.id.tvVoltage)
        tvCurrent = findViewById(R.id.tvCurrent)
        tvHealth = findViewById(R.id.tvHealth)
        tvStatus = findViewById(R.id.tvStatus)
        tvTech = findViewById(R.id.tvTech)
        tvDevice = findViewById(R.id.tvDevice)
        tvUpdated = findViewById(R.id.tvUpdated)
    }

    private fun showDeviceInfo() {
        tvDevice.text = getString(
            R.string.device_info,
            Build.MANUFACTURER,
            Build.MODEL,
            Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT
        )
    }

    private fun refresh() {
        // ACTION_BATTERY_CHANGED 是 sticky 广播，传 null receiver 即可取回最近一次值
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY).orEmpty()

        tvTemp.text = if (tempRaw == Int.MIN_VALUE) NA
        else String.format(Locale.getDefault(), "%.1f °C", tempRaw / 10.0)

        tvLevel.text = if (level < 0 || scale <= 0) NA
        else String.format(Locale.getDefault(), "%d%%  (%d / %d)", level * 100 / scale, level, scale)

        tvVoltage.text = if (voltageMv < 0) NA
        else String.format(Locale.getDefault(), "%.3f V", voltageMv / 1000.0)

        tvCurrent.text = readCurrent()
        tvHealth.text = describeHealth(health)
        tvStatus.text = describeStatus(status)
        tvTech.text = tech.ifBlank { NA }

        tvUpdated.text = getString(R.string.updated_at, timeFormat.format(Date()))
    }

    /** 瞬时电流（µA）。正值 = 充电，负值 = 放电。 */
    private fun readCurrent(): String {
        val manager = getSystemService(BATTERY_SERVICE) as? BatteryManager ?: return NA
        val microAmps = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (microAmps == Int.MIN_VALUE) return NA

        val milliAmps = microAmps / 1000.0
        val direction = if (milliAmps > 0) "充电" else "放电"
        return String.format(Locale.getDefault(), "%.0f mA (%s)", abs(milliAmps), direction)
    }

    private fun describeHealth(health: Int): String = when (health) {
        BatteryManager.BATTERY_HEALTH_UNKNOWN -> "未知"
        BatteryManager.BATTERY_HEALTH_GOOD -> "良好"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "过热"
        BatteryManager.BATTERY_HEALTH_DEAD -> "损坏"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "过压"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "未指定故障"
        BatteryManager.BATTERY_HEALTH_COLD -> "过冷"
        else -> NA
    }

    private fun describeStatus(status: Int): String = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
        BatteryManager.BATTERY_STATUS_FULL -> "已充满"
        BatteryManager.BATTERY_STATUS_UNKNOWN -> "未知"
        else -> NA
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 1000L
        private const val NA = "—"
    }
}
