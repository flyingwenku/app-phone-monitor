package com.fred.phonemonitor.core

/**
 * 统一数据源接口。
 *
 * 新增一个采集项 = 新增一个实现类，不需要改动 UI 与调度逻辑，
 * 这是「环境层与应用层解耦」在应用内部的落地。
 */
interface MetricSource {

    /** 分组标识，UI 按 key 取数据 */
    val key: String

    /** 分组显示名 */
    val label: String

    /** 读取当前值。允许阻塞，但应尽量轻量（1 秒调度一次）。 */
    fun read(): List<Metric>
}
