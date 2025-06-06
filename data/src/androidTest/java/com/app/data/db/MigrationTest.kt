package com.app.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.app.data.local.db.AppDb
import com.app.data.local.db.MIGRATION_1_2
import com.app.data.local.db.MIGRATION_2_3
import net.sqlcipher.database.SupportFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * @file MigrationTest.kt
 * @brief Verifica que las migraciones de Room se ejecutan correctamente.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDb::class.java.canonicalName
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To3() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test-db"

        // Crea base versión 1
        helper.createDatabase(dbName, 1).apply { close() }

        // Abre base con migraciones 1_2 y 2_3
        Room.databaseBuilder(context, AppDb::class.java, dbName)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .openHelperFactory(SupportFactory(byteArrayOf()))
            .build()
            .apply { close() }
    }
}
