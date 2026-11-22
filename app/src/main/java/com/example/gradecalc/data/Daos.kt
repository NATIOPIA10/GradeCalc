package com.example.gradecalc.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters ORDER BY year, term, id")
    fun observeAll(): Flow<List<SemesterEntity>>

    @Insert
    suspend fun insert(semester: SemesterEntity): Long

    @Delete
    suspend fun delete(semester: SemesterEntity)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY id")
    fun observeAll(): Flow<List<CourseEntity>>

    @Insert
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Delete
    suspend fun delete(course: CourseEntity)
}

@Dao
interface GradeScaleDao {
    @Query("SELECT * FROM grade_scale ORDER BY sortOrder")
    fun observeAll(): Flow<List<GradeScaleEntry>>

    @Query("SELECT COUNT(*) FROM grade_scale")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<GradeScaleEntry>)

    @Update
    suspend fun update(entry: GradeScaleEntry)

    @Query("DELETE FROM grade_scale")
    suspend fun deleteAll()
}
