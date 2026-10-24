package com.fred.phonemonitor.core

/** 一条指标记录。key 用于去重与分组，label 显示名，value 已格式化好的字符串。 */
data class Metric(
    val key: String,
    val label: String,
    val value: String
)
