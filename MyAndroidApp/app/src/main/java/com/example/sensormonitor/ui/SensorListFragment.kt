package com.example.sensormonitor.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sensormonitor.R
import com.example.sensormonitor.model.SensorData
import com.example.sensormonitor.util.SensorDataManager
import android.hardware.Sensor
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment to display list of all available sensors
 */
class SensorListFragment : Fragment() {
    
    private var sensorDataManager: SensorDataManager? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: SensorListAdapter? = null
    private var selectedSensorType: Int? = null
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    interface OnSensorSelectedListener {
        fun onSensorSelected(sensorType: Int, sensorName: String)
    }
    
    private var listener: OnSensorSelectedListener? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorDataManager = SensorDataManager(requireContext())
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sensor_list, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerViewSensors)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        
        val sensors = sensorDataManager?.getAvailableSensors() ?: emptyList()
        adapter = SensorListAdapter(sensors, sensorDataManager!!) { sensor ->
            selectedSensorType = sensor.type
            listener?.onSensorSelected(sensor.type, sensorDataManager!!.getSensorTypeName(sensor.type))
        }
        recyclerView?.adapter = adapter
    }
    
    fun setOnSensorSelectedListener(listener: OnSensorSelectedListener) {
        this.listener = listener
    }
    
    fun updateSensorData(sensorData: SensorData) {
        adapter?.updateSensorData(sensorData)
    }
}

/**
 * Adapter for sensor list
 */
class SensorListAdapter(
    private val sensors: List<Sensor>,
    private val dataManager: SensorDataManager,
    private val onItemClick: (Sensor) -> Unit
) : RecyclerView.Adapter<SensorListAdapter.ViewHolder>() {
    
    private val sensorDataMap = mutableMapOf<Int, SensorData>()
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sensorName: TextView = itemView.findViewById(R.id.sensorName)
        val sensorType: TextView = itemView.findViewById(R.id.sensorType)
        val sensorValues: TextView = itemView.findViewById(R.id.sensorValues)
        val sensorUnit: TextView = itemView.findViewById(R.id.sensorUnit)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sensor = sensors[position]
        holder.sensorName.text = dataManager.getSensorTypeName(sensor.type)
        holder.sensorType.text = "Type: ${sensor.type}"
        
        val data = sensorDataMap[sensor.type]
        if (data != null) {
            holder.sensorValues.text = data.getValueString()
            holder.sensorUnit.text = data.unit
        } else {
            holder.sensorValues.text = "--"
            holder.sensorUnit.text = dataManager.getSensorUnit(sensor.type)
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(sensor)
        }
    }
    
    override fun getItemCount(): Int = sensors.size
    
    fun updateSensorData(data: SensorData) {
        sensorDataMap[data.sensorType] = data
        // Find position and notify
        val position = sensors.indexOfFirst { it.type == data.sensorType }
        if (position >= 0) {
            notifyItemChanged(position)
        }
    }
}
