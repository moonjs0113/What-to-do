package com.example.whattodo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whattodo.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val textarr = arrayListOf<String>("우선도", "마감일", "검색", "환경설정")
//    val imgarr = arrayListOf<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        layoutInit()
    }

    fun layoutInit() {
        binding.viewPager.adapter = MainViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) {
                tab, pos ->
            tab.text = textarr[pos]
//            tab.setIcon(imgarr[pos])
        }.attach()
    }
}