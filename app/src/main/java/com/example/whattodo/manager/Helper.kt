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
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val str = this.substring(0, 16)
    return LocalDateTime.parse(str, formatter)
}

fun Date.toString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return dateFormat.format(this)
}

