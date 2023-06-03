package com.example.whattodo.manager.Persistence

import android.content.Context
import android.content.SharedPreferences
import com.example.whattodo.manager.room.ToDoDatabase

class SharedPreferencesManager {
    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences

    companion object {
        const val preferencesName = "SHARED_PREFERENCES"
    }

    /// Activity or Fragment가 변경될 때 호출하여 Context정보를 교체합니다.
    fun registerContext(context: Context) {
        this.context = context
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

}