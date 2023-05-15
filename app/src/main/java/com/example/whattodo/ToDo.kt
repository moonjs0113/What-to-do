package com.example.whattodo

import java.text.SimpleDateFormat
import java.util.*

data class ToDo (var explanation : String, var deadLine : Date, var time_taken : Float , var importance : Int ) {
    companion object {
        private val dataformat = SimpleDateFormat("yyyy-MM-dd")
        public var previewData =
            arrayListOf<ToDo>(
                ToDo("example1", dataformat.parse("2023-05-26"), 12f, 5),
                ToDo("example2", dataformat.parse("2023-06-01"), 5f, 4),
                ToDo("example3", dataformat.parse("2023-06-04"), 4f, 3),
                ToDo("example4", dataformat.parse("2023-06-15"), 3f, 2),
                ToDo("example5", dataformat.parse("2023-06-30"), 1f, 1)
            )
    }
}
// explanation : 할 일에 대한 설명
// deadLine : 마감일
// time_taken : 소요 시간
// priority : 중요도 (1~10 사이 정수)