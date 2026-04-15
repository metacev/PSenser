package com.example.sensormonitor.ui

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.sensormonitor.ui.fragments.SensorsFragment
import com.example.sensormonitor.ui.fragments.HardwareFragment
import com.example.sensormonitor.ui.fragments.SystemFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        SensorsFragment::class.java,
        HardwareFragment::class.java,
        SystemFragment::class.java
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position].getConstructor().newInstance()
    }
}
