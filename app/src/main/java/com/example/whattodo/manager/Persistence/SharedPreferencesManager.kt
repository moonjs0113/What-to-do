package com.example.whattodo.manager.Persistence

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Color
import com.example.whattodo.manager.room.ToDoDatabase

class SharedPreferencesManager {
    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private lateinit var preferencesEditor: SharedPreferences.Editor

    companion object {
        const val preferencesName = "SHARED_PREFERENCES"
        const val priorityNameKey = "PRIOTIRY_KEY"
        const val timeValueKey = "TIME_VALUE"
        const val priorityValueKey = "PRIOTIRY_VALUE"
        const val notificationKey = "NOTIFICATION_KEY"
    }

    enum class Color(var code: String) {
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

    fun setColorCode(index: Int, newColorCode: String) {
        val colorValues = Color.values()
        val editor = preferences.edit()
        editor.putString("${colorValues[index]}", newColorCode)
        editor.apply()
    }

    enum class PriorityItem {
        TIME,
        IMPORTANCE
    }

    fun getPriorityItem(): Triple<PriorityItem, Int, Int> {
        val item = preferences.getString(priorityNameKey, "${PriorityItem.TIME}") ?: "${PriorityItem.TIME}"
        val priorityValue = preferences.getInt(priorityValueKey, 10) ?: 10
        val timeValue = preferences.getInt(timeValueKey, 10) ?: 10
        return Triple(PriorityItem.valueOf(item), priorityValue, timeValue)
    }

    fun setPriorityItem(item: PriorityItem, priorityValue: Int, timeValue: Int) {
        val editor = preferences.edit()
        editor.putString(priorityNameKey, "$item")
        editor.putInt(priorityValueKey, priorityValue)
        editor.putInt(timeValueKey, timeValue)
        editor.apply()
    }

    fun getNotificationValue(): Boolean {
        return preferences.getBoolean(notificationKey, true) ?: true
    }
    fun setNotification(isOn: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(notificationKey, isOn)
        editor.apply()
    }
}