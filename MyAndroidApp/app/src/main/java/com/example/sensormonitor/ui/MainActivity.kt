package com.example.sensormonitor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.sensormonitor.R
import com.example.sensormonitor.databinding.ActivityMainBinding
import com.example.sensormonitor.util.DeviceInfoManager
import com.example.sensormonitor.util.SensorDataManager
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorDataManager: SensorDataManager
    private lateinit var deviceInfoManager: DeviceInfoManager
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private val requiredPermissions = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize managers
        sensorDataManager = SensorDataManager(this)
        deviceInfoManager = DeviceInfoManager(this)

        // Setup UI
        setupToolbar()
        setupViewPager()
        checkAndRequestPermissions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Sensor Monitor"
        supportActionBar?.subtitle = "Android ${Build.VERSION.RELEASE}"
    }

    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Sensors"
                1 -> "Hardware"
                2 -> "System"
                else -> "Info"
            }
        }.attach()
    }

    private fun checkAndRequestPermissions() {
        requiredPermissions.clear()

        // Android 13+ (API 33+) - Notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Activity Recognition (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (requiredPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allGranted = true
        permissions.forEach { (permission, isGranted) ->
            if (!isGranted) {
                allGranted = false
                Toast.makeText(
                    this,
                    "Permission $permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (allGranted) {
            startMonitoring()
        } else {
            Toast.makeText(
                this,
                "Some permissions denied. Some features may not work.",
                Toast.LENGTH_LONG
            ).show()
            startMonitoring() // Still start with available permissions
        }
    }

    private fun startMonitoring() {
        lifecycleScope.launch {
            sensorDataManager.startListening()
        }
        updateDeviceInfo()
    }

    private fun updateDeviceInfo() {
        binding.apply {
            tvDeviceModel.text = deviceInfoManager.getDeviceModel()
            tvAndroidVersion.text = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            tvManufacturer.text = deviceInfoManager.getManufacturer()
            tvRamInfo.text = deviceInfoManager.getRamInfo()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorDataManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        sensorDataManager.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorDataManager.stopListening()
    }
}
