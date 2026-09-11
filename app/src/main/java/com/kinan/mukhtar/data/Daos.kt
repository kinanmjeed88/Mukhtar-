package com.kinan.mukhtar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun observeConfig(): Flow<AppConfigEntity?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: AppConfigEntity)

    @Query("DELETE FROM app_config")
    suspend fun clear()
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons ORDER BY fullName ASC")
    fun observeAll(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE isMarried = 1 ORDER BY fullName ASC")
    fun observeFamilies(): Flow<List<PersonEntity>>

    /** فحص التكرار: يستثني السجل الجاري تعديله */
    @Query("SELECT COUNT(*) FROM persons WHERE TRIM(fullName) = TRIM(:name) AND id != :excludeId")
    suspend fun countByExactName(name: String, excludeId: Long): Int

    @Query("SELECT * FROM persons WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PersonEntity?

    @Query("SELECT * FROM persons")
    suspend fun getAllOnce(): List<PersonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: PersonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(people: List<PersonEntity>)

    @Update
    suspend fun update(person: PersonEntity)

    @Delete
    suspend fun delete(person: PersonEntity)

    @Query("DELETE FROM persons")
    suspend fun clear()
}
