package com.app.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v1 → v2 · añadimos columna createdLocalAt a habits */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE habits ADD COLUMN meta_createdLocalAt INTEGER NOT NULL DEFAULT 0
        """.trimIndent())                             // ALTER en Room ↓ docs oficiales :contentReference[oaicite:6]{index=6}
    }
}

/** v2 → v3 · index para acelerar ORDER BY timestamp */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE INDEX IF NOT EXISTS index_activities_timestamp ON activities(timestamp)""")
    }
}

/** v3 → v4 · nueva columna para tutorial */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE onboarding_status ADD COLUMN hasCompletedTutorial INTEGER NOT NULL DEFAULT 0")
    }
}
