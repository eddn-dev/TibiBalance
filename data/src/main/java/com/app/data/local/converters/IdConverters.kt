package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId

object IdConverters {
    @TypeConverter fun habitIdToString(id: HabitId?)   = id?.raw
    @TypeConverter fun stringToHabitId(v: String?)    = v?.let(::HabitId)

    @TypeConverter fun activityIdToString(id: ActivityId?) = id?.raw
    @TypeConverter fun stringToActivityId(v: String?)     = v?.let(::ActivityId)
}