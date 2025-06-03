package com.app.data.remote.datasource

import com.app.domain.entities.DailyTip

interface DailyTipsRemoteDataSource {
    suspend fun fetchAll(): List<DailyTip>
}