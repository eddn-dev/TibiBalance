package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.domain.enums.ActivityType
import com.app.domain.enums.HabitCategory
import com.app.domain.enums.NotifChannel
import com.app.domain.enums.NotifMode
import com.app.domain.enums.PeriodUnit
import com.app.domain.enums.SessionUnit

object EnumConverters {
    private inline fun <reified E : Enum<E>> toString(value: E?): String? = value?.name
    private inline fun <reified E : Enum<E>> fromString(value: String?): E? =
        value?.let { enumValueOf<E>(it) }

    @TypeConverter fun catToString (v: HabitCategory?) = toString(v)
    @TypeConverter fun stringToCat (v: String?)       = fromString<HabitCategory>(v)

    @TypeConverter fun sUnitToString(v: SessionUnit?) = toString(v)
    @TypeConverter fun stringToSUnit(v: String?)      = fromString<SessionUnit>(v)

    @TypeConverter fun pUnitToString(v: PeriodUnit?)  = toString(v)
    @TypeConverter fun stringToPUnit(v: String?)      = fromString<PeriodUnit>(v)

    @TypeConverter fun aTypeToString(v: ActivityType?)= toString(v)
    @TypeConverter fun stringToAType(v: String?)      = fromString<ActivityType>(v)

    @TypeConverter fun nModeToString(v: NotifMode?)   = toString(v)
    @TypeConverter fun stringToNMode(v: String?)      = fromString<NotifMode>(v)

    @TypeConverter fun nChanToString(v: NotifChannel?)= toString(v)
    @TypeConverter fun stringToNChan(v: String?)      = fromString<NotifChannel>(v)
}