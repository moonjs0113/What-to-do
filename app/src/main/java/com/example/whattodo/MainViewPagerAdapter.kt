package com.example.whattodo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> PriorityFragment()
            1-> DeadlineFragment()
            2 -> SearchFragment()
            3 -> SettingFragment()
            else -> PriorityFragment()
        }
    }
}