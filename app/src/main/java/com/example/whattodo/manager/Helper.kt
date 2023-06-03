package com.example.whattodo.manager.Persistence

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun String.toDate(): Date {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return dateFormat.parse(this) ?: Date()
}

fun String.toLocalDateTime(): LocalDateTime {
    val dateTimeString = "2021-11-05 13:47:13"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.parse(dateTimeString, formatter)
}

fun Date.toString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return dateFormat.format(this)
}

