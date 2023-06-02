package com.example.whattodo.manager.Persistence

import android.content.Context
import com.example.whattodo.ToDo
import com.example.whattodo.manager.room.RoomManager

class PersistenceService {
    private var sharedPreferencesManger = SharedPreferencesManager()
    private var roomManager = RoomManager()

    companion object {
        val share: PersistenceService = PersistenceService()
    }

    // SharedPreferences
//    fun get

    // Room
    fun registerContext(context: Context) {
        roomManager.registerContext(context)
    }

    fun getAllTodo(context: Context): ArrayList<ToDo> {
        return roomManager.getAllToDo()
    }

    fun insertTodo(toDo: ToDo) {
        roomManager.insertToDo(toDo)
    }

    fun updateTodo(toDo: ToDo) {
        roomManager.updateToDo(toDo)
    }

    fun deleteTodo(toDo: ToDo) {
        roomManager.deleteToDo(toDo)
    }

    /// Context에서 호출하여 테스트해볼 수 있습니다. 테스트 결과는 Logcat을 통해 확인할 수 있습니다.
    fun testRoomManager(context: Context) {
        roomManager.test(context)
    }
}