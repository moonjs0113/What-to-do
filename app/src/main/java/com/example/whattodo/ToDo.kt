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
    @ColumnInfo(name = "priority") var priority: Float
    ) {

    constructor(
        explanation: String,
        deadLine: String,
        time_taken: Float,
        importance: Int,
        priority: Float
    ): this(0, explanation, deadLine, time_taken, importance, priority)

    companion object {
        var previewData =
            arrayListOf<ToDo>(
                ToDo("example1", "2023-06-29T00:00", 12f, 5 , 0f) ,
                ToDo("ㅋexample1-1", "2023-06-29T00:00", 10f, 9, 0f) ,
                ToDo("Example1-2", "2023-06-30T00:00", 8f, 7, 0f) ,
                ToDo("Aexample1-3", "2023-07-31T00:00", 4f, 4, 0f) ,
                ToDo("d_example2", "2023-07-01T00:00", 5f, 4, 5.0f),
                ToDo("-example3", "2023-07-04T00:00", 4f, 3, 3.0f),
                ToDo("a-example4", "2023-07-15T00:00", 3f, 2,4.0f),
                ToDo("5-example5", "2023-07-30T00:00", 1f, 1,4.0f)
            )
    }
}
// explanation : 할 일에 대한 설명
// deadLine : 마감일
// time_taken : 소요 시간
// priority : 중요도 (1~10 사이 정수)

 // Default: Class Name
//@Entity