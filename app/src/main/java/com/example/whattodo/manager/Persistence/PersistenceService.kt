package com.example.whattodo.manager.Persistence

import android.content.Context
import com.example.whattodo.ToDo
import com.example.whattodo.manager.room.RoomManager
import com.example.whattodo.manager.Persistence.SharedPreferencesManager.PriorityItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PersistenceService {
    private var sharedPreferencesManger = SharedPreferencesManager()
    private var roomManager = RoomManager()

    companion object {
        val share: PersistenceService = PersistenceService()
    }

    // SharedPreferences
    fun getColorArray() : ArrayList<String> = sharedPreferencesManger.getColorCodeArray()

    fun setColor(index: Int, colorCode: String) {
        sharedPreferencesManger.setColorCode(index, colorCode)
    }

    fun getPriorityItem(): Triple<PriorityItem, Int, Int> = sharedPreferencesManger.getPriorityItem()

    fun setPriorityItem(index: Int, priorityValue: Int, timeValue: Int) {
        sharedPreferencesManger.setPriorityItem(PriorityItem.values()[index], priorityValue, timeValue)
    }

    fun getNotificationValue(): Boolean = sharedPreferencesManger.getNotificationValue()

    fun setNotificationValue(isOn: Boolean) {
        sharedPreferencesManger.setNotification(isOn)
    }
    // Room
    fun registerContext(context: Context) {
        roomManager.registerContext(context)
        sharedPreferencesManger.registerContext(context)
    }

    fun getAllTodo(): ArrayList<ToDo> = roomManager.getAllToDo()

    fun insertTodo(toDo: ToDo) {
        CoroutineScope(Dispatchers.IO).launch {
            roomManager.insertToDo(toDo)
        }
    }

    fun updateTodo(toDo: ToDo) {
        CoroutineScope(Dispatchers.IO).launch {
            roomManager.updateToDo(toDo)
        }
    }

    fun deleteTodo(toDo: ToDo) {
        CoroutineScope(Dispatchers.IO).launch {
            roomManager.deleteToDo(toDo)
        }
    }

    /// Context에서 호출하여 테스트해볼 수 있습니다. 테스트 결과는 Logcat을 통해 확인할 수 있습니다.
    fun testRoomManager(context: Context) {
//        roomManager.test(context)
    }
}