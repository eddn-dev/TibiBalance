package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId

object IdConverters {
    @TypeConverter fun habitIdToString(v: HabitId?)   = v?.raw
    @TypeConverter fun stringToHabitId(v: String?)    = v?.let(::HabitId)

    @TypeConverter fun activityIdToString(v: ActivityId?) = v?.raw
    @TypeConverter fun stringToActivityId(v: String?)     = v?.let(::ActivityId)
}