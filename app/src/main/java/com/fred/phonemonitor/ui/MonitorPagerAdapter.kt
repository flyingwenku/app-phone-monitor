package com.fred.phonemonitor.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/** Tab 定义：标题 + 该页要展示的数据源 key 列表。 */
data class TabSpec(
    val title: String,
    val keys: List<String>
)

class MonitorPagerAdapter(
    activity: FragmentActivity,
    private val tabs: List<TabSpec>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment =
        MetricFragment.newInstance(tabs[position].keys)
}
