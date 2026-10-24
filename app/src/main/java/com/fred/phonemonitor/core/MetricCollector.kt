package com.fred.phonemonitor.core

/** 聚合所有数据源。单个数据源抛异常不影响其他数据源。 */
class MetricCollector(private val sources: List<MetricSource>) {

    fun readAll(): Map<String, List<Metric>> {
        val result = LinkedHashMap<String, List<Metric>>()
        for (source in sources) {
            result[source.key] = try {
                source.read()
            } catch (e: Exception) {
                listOf(Metric("${source.key}_error", "读取失败", e.javaClass.simpleName))
            }
        }
        return result
    }
}
