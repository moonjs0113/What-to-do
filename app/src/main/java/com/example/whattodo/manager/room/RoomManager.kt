package com.example.whattodo.manager.room

import android.content.Context
import android.util.Log
import com.example.whattodo.ToDo
import kotlinx.coroutines.*

class RoomManager {
    private lateinit var todoDB: ToDoDatabase
    private lateinit var context: Context

    /// Activity or Fragment가 변경될 때 호출하여 Context정보를 교체합니다.
    fun registerContext(context: Context) {
        this.context = context
        CoroutineScope(Dispatchers.IO).launch {
            todoDB = ToDoDatabase.getDatabase(this@RoomManager.context)
        }
    }

    fun getAllToDo(): ArrayList<ToDo> = todoDB.toDoDAO().getAllTodo() as ArrayList<ToDo>

    fun insertToDo(toDo: ToDo) {
        CoroutineScope(Dispatchers.IO).launch {
            todoDB.toDoDAO().insertToDo(toDo)
        }
    }

    fun updateToDo(toDo: ToDo) {
        CoroutineScope(Dispatchers.IO).launch {
            todoDB.toDoDAO().updateToDo(toDo)
        }
    }

    fun deleteToDo(toDo: ToDo) {
        CoroutineScope(Dispatchers.IO).launch {
            todoDB.toDoDAO().deleteToDo(toDo)
        }
    }

    fun findTodoFromExplanation(keyword: String): ArrayList<ToDo> = todoDB.toDoDAO().findToDo(keyword) as ArrayList<ToDo>

    fun test(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val logTag = "ROOM_TEST"
            registerContext(context)
            Log.i(logTag, "Register Context Success")
            Log.i(logTag, "Get All To Do: ${getAllToDo()}")
            var testVO = ToDo(0,"테스트 객체입니다.", "2023-06-01", 10.0f, 3, 5.0f)
            var testVO_2 = ToDo(0,"테스트 객체입니다.", "2023-06-01", 10.0f, 3, 5.0f)
            insertToDo(testVO)
            delay(100)
            insertToDo(testVO_2)
            delay(100)
            Log.i(logTag, "Inesrt Test: ${getAllToDo()}")
            testVO = getAllToDo().first()
            var findTodo = findTodoFromExplanation(".")
            Log.i(logTag, "Find Test(keyword:객체): ${findTodo}")
            testVO.explanation = "업데이트 테스트 입니다"
            Log.i(logTag, "Test: $testVO")
            updateToDo(testVO)
            delay(100)
            Log.i(logTag, "Update Test: ${getAllToDo()}")
            findTodo = findTodoFromExplanation("객체")
            Log.i(logTag, "Find Test(keyword:객체): $findTodo")
            deleteToDo(testVO)
            delay(100)
            Log.i(logTag, "Delete Test: ${getAllToDo()}")
        }
    }

}