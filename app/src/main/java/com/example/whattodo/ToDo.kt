package com.example.whattodo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat

@Entity(tableName = "todos")
data class ToDo(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "explanation") var explanation: String,
    @ColumnInfo(name = "deadLine") var deadLine: String,
    @ColumnInfo(name = "timeTaken") var time_taken: Float,
    @ColumnInfo(name = "importance") var importance: Int,
    @ColumnInfo(name = "priority") var priority: Float,
    @ColumnInfo(name = "isComplete") var isComplete: Boolean
    ) {

    constructor(
        explanation: String,
        deadLine: String,
        time_taken: Float,
        importance: Int,
        priority: Float
    ): this(0, explanation, deadLine, time_taken, importance, priority, false)

    companion object {

    }
}
// explanation : 할 일에 대한 설명
// deadLine : 마감일
// time_taken : 소요 시간
// priority : 중요도 (1~10 사이 정수)

 // Default: Class Name
//@Entity