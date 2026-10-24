package com.fred.phonemonitor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fred.phonemonitor.MainActivity
import com.fred.phonemonitor.R
import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricUpdateListener

/**
 * 通用指标页：接收一组数据源 key，只展示这些 key 的指标。
 * 所有 Tab 复用同一个 Fragment，仅参数不同。
 */
class MetricFragment : Fragment(), MetricUpdateListener {

    private lateinit var adapter: MetricAdapter
    private var keys: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keys = arguments?.getStringArrayList(ARG_KEYS) ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_metrics, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = MetricAdapter()
        recycler.adapter = adapter

        val host = activity as? MainActivity
        host?.addListener(this)
        // 立即填充一次，避免刚切到该页时空白等待 1 秒
        host?.lastData?.let { onMetricsUpdated(it) }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.removeListener(this)
        super.onDestroyView()
    }

    override fun onMetricsUpdated(data: Map<String, List<Metric>>) {
        if (!isAdded) return
        val items = keys.flatMap { key -> data[key] ?: emptyList() }
        adapter.submitList(items)
    }

    companion object {
        private const val ARG_KEYS = "arg_keys"

        fun newInstance(keys: List<String>): MetricFragment =
            MetricFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_KEYS, ArrayList(keys))
                }
            }
    }
}
