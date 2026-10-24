package com.fred.phonemonitor.core

/** 每秒刷新回调，Fragment 实现它来接收数据。 */
interface MetricUpdateListener {
    fun onMetricsUpdated(data: Map<String, List<Metric>>)
}
