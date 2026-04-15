package com.example.sensormonitor.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sensormonitor.databinding.ItemSensorBinding
import com.example.sensormonitor.model.SensorData
import com.example.sensormonitor.util.SensorDataManager

class SensorDataAdapter : ListAdapter<SensorData, SensorDataAdapter.SensorViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val binding = ItemSensorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SensorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SensorViewHolder(private val binding: ItemSensorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sensorData: SensorData) {
            binding.apply {
                textSensorName.text = sensorData.name
                textSensorType.text = sensorData.type
                textSensorVendor.text = "厂商：${sensorData.vendor}"
                textSensorValues.text = formatValues(sensorData.values)
                textSensorUnit.text = sensorData.unit
            }
        }

        private fun formatValues(values: List<Float>): String {
            if (values.isEmpty()) return "无数据"
            return values.joinToString(" | ") { value ->
                "%.2f".format(value)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SensorData>() {
        override fun areItemsTheSame(oldItem: SensorData, newItem: SensorData): Boolean {
            return oldItem.name == newItem.name && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: SensorData, newItem: SensorData): Boolean {
            return oldItem == newItem
        }
    }
}
