package com.example.whattodo.manager.room

import androidx.room.*
import com.example.whattodo.ToDo

@Dao
interface ToDoDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertToDo(todo: ToDo)

    @Delete
    fun deleteToDo(todo: ToDo)

    @Update
    fun updateToDo(todo: ToDo)

    @Query("SELECT * FROM todos")
    fun getAllTodo(): List<ToDo>


    @Query("SELECT * FROM todos WHERE explanation LIKE '%' || :keyword || '%'")
    fun findToDo(keyword: String): List<ToDo>
}