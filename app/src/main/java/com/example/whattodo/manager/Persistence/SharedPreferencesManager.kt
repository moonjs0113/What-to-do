package com.example.whattodo.manager.Persistence

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import com.example.whattodo.manager.room.ToDoDatabase

class SharedPreferencesManager {
    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences

    companion object {
        const val preferencesName = "SHARED_PREFERENCES"
    }

    enum class Color(val code: String) {
        Priority_0("e03b22"),
        Priority_1("eff238"),
        Priority_2("24f064"),
    }

    /// Activity or Fragment가 변경될 때 호출하여 Context정보를 교체합니다.
    fun registerContext(context: Context) {
        this.context = context
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    fun getColorCodeArray(): ArrayList<String> {
        val colorValues = Color.values()
        return ArrayList(
            colorValues
                .indices
                .map { index ->
                    val defaultCode = colorValues[index].code
                    preferences.getString("${colorValues[index]}", defaultCode)
                        ?: defaultCode
                }
        )
    }
}