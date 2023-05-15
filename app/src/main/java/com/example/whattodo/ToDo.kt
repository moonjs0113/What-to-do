package com.example.whattodo

import java.util.*

data class ToDo (var explanation : String, var deadLine : Date, var time_taken : Float , var importance : Int )
// explanation : 할 일에 대한 설명
// deadLine : 마감일
// time_taken : 소요 시간
// priority : 중요도 (1~10 사이 정수)