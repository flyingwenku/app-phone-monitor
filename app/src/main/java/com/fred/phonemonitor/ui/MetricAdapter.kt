package com.fred.phonemonitor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fred.phonemonitor.R
import com.fred.phonemonitor.core.Metric

/** 指标列表适配器。数据量大（传感器 / 探测面板），用 RecyclerView 保证滚动流畅。 */
class MetricAdapter : RecyclerView.Adapter<MetricAdapter.ViewHolder>() {

    private var items: List<Metric> = emptyList()

    fun submitList(newItems: List<Metric>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_metric, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val label: TextView = view.findViewById(R.id.tvLabel)
        private val value: TextView = view.findViewById(R.id.tvValue)

        fun bind(metric: Metric) {
            label.text = metric.label
            value.text = metric.value
        }
    }
}
