package com.example.gradecalc.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v1 -> v2: courses can store a mark out of 100. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE courses ADD COLUMN scorePercent REAL")
    }
}

/**
 * v2 -> v3: semesters get a year and a semester number. Existing semesters keep their typed name
 * and get year 0 / term 0, which means "legacy, not from the picker".
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE semesters ADD COLUMN year INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE semesters ADD COLUMN term INTEGER NOT NULL DEFAULT 0")
    }
}
