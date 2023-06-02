package com.example.whattodo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "todos")
data class ToDo (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "explanation") var explanation : String,
    @ColumnInfo(name = "deadLine") var deadLine : String,
    @ColumnInfo(name = "timeTaken") var time_taken : Float,
    @ColumnInfo(name = "importance") var importance : Int,
    @ColumnInfo(name = "priority") var priority : Float
    ) {

    constructor(
        explanation : String,
        deadLine : String,
        time_taken : Float,
        importance : Int,
        priority : Float
    ): this(0, explanation, deadLine, time_taken, importance, priority)

    companion object {
        private val dataformat = SimpleDateFormat("yyyy-MM-dd")
        public var previewData =
            arrayListOf<ToDo>(
                ToDo("example1", "2023-05-26", 12f, 5, 12.0f),
                ToDo("example2", "2023-06-01", 5f, 4, 1.0f),
                ToDo("example3", "2023-06-04", 4f, 3, 3.0f),
                ToDo("example4", "2023-06-15", 3f, 2,4.0f),
                ToDo("example5", "2023-06-30", 1f, 1,4.0f)
            )
    }
}

// explanation : 할 일에 대한 설명
// deadLine : 마감일
// time_taken : 소요 시간
// priority : 중요도 (1~10 사이 정수)

 // Default: Class Name
//@Entity