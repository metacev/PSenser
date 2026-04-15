package com.example.sensormonitor.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sensormonitor.R
import com.example.sensormonitor.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Main Activity - Sensor Monitor App
 * Displays real-time sensor data and device information
 */
class MainActivity : AppCompatActivity(), SensorListFragment.OnSensorSelectedListener {
    
    private lateinit var binding: ActivityMainBinding
    
    private val tabTitles = listOf("Sensors", "Device Info")
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Some permissions denied. Some features may not work.", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Sensor Monitor"
        supportActionBar?.subtitle = "Real-time Device Monitoring"
        
        checkPermissions()
        setupViewPager()
    }
    
    private fun checkPermissions() {
        val missingPermissions = PermissionHelper.getRequiredPermissions().filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    
    private fun setupViewPager() {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabTitles.size
            
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> SensorListFragment()
                    1 -> DeviceInfoFragment()
                    else -> SensorListFragment()
                }
            }
        }
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
        
        // Set listener for sensor selection
        val sensorListFragment = supportFragmentManager.findFragmentByTag("f0") as? SensorListFragment
        sensorListFragment?.setOnSensorSelectedListener(this)
    }
    
    override fun onSensorSelected(sensorType: Int, sensorName: String) {
        // Navigate to detail view or show bottom sheet
        val detailFragment = SensorDetailFragment.newInstance(sensorType, sensorName)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, detailFragment)
            .addToBackStack(null)
            .commit()
    }
    
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
