package com.app.tibibalance.ui.permissions

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord

/**
 * Permisos runtime que se solicitan a través de Health Connect.
 * Cada elemento es un String que identifica un tipo de dato.
 */
val HEALTH_CONNECT_READ_PERMISSIONS: Set<String> = setOf(
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
    HealthPermission.getReadPermission(HeartRateRecord::class)
)
