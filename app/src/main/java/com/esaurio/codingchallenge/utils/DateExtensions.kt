package com.esaurio.codingchallenge.utils

import java.text.SimpleDateFormat
import java.util.*

fun createDate(rawString : String, format : String) : Date? {
    return SimpleDateFormat(format, Locale.getDefault()).parse(rawString)
}

fun Date.toString(format : String) : String {
    return SimpleDateFormat(format, Locale.getDefault()).format(this)
}