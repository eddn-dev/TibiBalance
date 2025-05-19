package com.app.data.local.converters

import androidx.room.TypeConverter   // 👈  <---------------------

object IntSetConverter {
    @TypeConverter
    fun fromString(value: String?): Set<Int> =
        value?.takeIf { it.isNotBlank() }
            ?.split(',')?.map { it.toInt() }?.toSet() ?: emptySet()

    @TypeConverter
    fun toString(set: Set<Int>?): String =
        set?.joinToString(",") ?: ""
}

object StringSetConverter {
    @TypeConverter
    fun fromString(value: String?): Set<String> =
        value?.takeIf { it.isNotBlank() }?.split('|')?.toSet() ?: emptySet()

    @TypeConverter
    fun toString(set: Set<String>?): String =
        set?.joinToString("|") ?: ""
}
