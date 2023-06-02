package com.example.whattodo.manager.room

import com.example.whattodo.ToDo
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ToDo::class], version = 1)
abstract class ToDoDatabase: RoomDatabase() {
    abstract fun toDoDAO(): ToDoDAO

    companion object {
        private var INSTANCE: ToDoDatabase? = null

        fun getDatabase(context: Context): ToDoDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            val instance = Room.databaseBuilder(
                context,
                ToDoDatabase::class.java,
                "todoDB"
            ).build()

            INSTANCE = instance
            return instance
        }
    }
}