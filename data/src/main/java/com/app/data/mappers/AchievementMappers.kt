package com.app.data.mappers

import com.app.data.local.entities.AchievementEntity
import com.app.domain.common.SyncMeta
import com.app.domain.entities.Achievement
import com.app.domain.ids.AchievementId
import kotlinx.datetime.Instant

object AchievementMappers {

    /* Room → Domain */
    fun AchievementEntity.toDomain() = Achievement(
        id          = AchievementId(id),
        name        = name,
        description = description,
        progress    = progress,
        unlocked    = unlocked,
        unlockDate  = unlockDate,
        meta        = meta
    )

    /* Domain → Room */
    fun Achievement.toEntity() = AchievementEntity(
        id          = id.raw,
        name        = name,
        description = description,
        progress    = progress,
        unlocked    = unlocked,
        unlockDate  = unlockDate,
        meta        = meta
    )
}
