package com.example.whattodo.manager.Persistence

import java.text.SimpleDateFormat
import java.util.*

fun String.toDate(): Date {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return dateFormat.parse(this) ?: Date()
}

fun Date.toString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return dateFormat.format(this)
}