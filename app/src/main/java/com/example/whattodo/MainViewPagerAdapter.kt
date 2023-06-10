package com.example.whattodo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whattodo.search.SearchFragment

class MainViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    val priorityFragment = PriorityFragment()
    val deadlineFragment = DeadlineFragment()
    val searchFragment = SearchFragment()
    val settingFragment = SettingFragment()


    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> priorityFragment
            1-> deadlineFragment
            2 -> searchFragment
            3 -> settingFragment
            else -> priorityFragment
        }
    }
}

// main branch
// -> $ git add .
// -> $ git commit -m ""

