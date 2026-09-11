package com.kinan.mukhtar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [AppConfigEntity::class, PersonEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): AppConfigDao
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /** ترقية غير مدمّرة: تحافظ على بيانات المستخدم الحالية */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_config ADD COLUMN mukhtarName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE persons ADD COLUMN gender TEXT NOT NULL DEFAULT 'ذكر'")
                db.execSQL("ALTER TABLE persons ADD COLUMN phoneNumber TEXT")
            }
        }

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mukhtar.db"
            ).addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}
