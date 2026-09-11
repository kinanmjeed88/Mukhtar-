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

    /** بحث موسع: يشمل اسم الزوجة فيعيد ملف الزوج عند البحث باسمها */
    @Query("""
        SELECT * FROM persons
        WHERE fullName LIKE '%' || :q || '%'
           OR IFNULL(spouseName, '') LIKE '%' || :q || '%'
        ORDER BY fullName ASC
    """)
    fun searchAll(q: String): Flow<List<PersonEntity>>

    @Query("""
        SELECT * FROM persons
        WHERE isMarried = 1
          AND (fullName LIKE '%' || :q || '%'
               OR IFNULL(spouseName, '') LIKE '%' || :q || '%')
        ORDER BY fullName ASC
    """)
    fun searchFamilies(q: String): Flow<List<PersonEntity>>

    /** دليل الهاتف: من لديهم رقم فقط، مع بحث بالاسم أو الرقم */
    @Query("""
        SELECT * FROM persons
        WHERE phoneNumber IS NOT NULL AND TRIM(phoneNumber) != ''
          AND (fullName LIKE '%' || :q || '%' OR phoneNumber LIKE '%' || :q || '%')
        ORDER BY fullName ASC
    """)
    fun searchPhoneDirectory(q: String): Flow<List<PersonEntity>>

    // ==================== الإحصائيات ====================

    @Query("SELECT COUNT(*) FROM persons")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM persons WHERE isMarried = 1")
    fun countFamilies(): Flow<Int>

    @Query("SELECT COUNT(*) FROM persons WHERE gender = :gender")
    fun countByGender(gender: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM persons WHERE phoneNumber IS NOT NULL AND TRIM(phoneNumber) != ''")
    fun countWithPhone(): Flow<Int>

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
