package com.kinan.mukhtar.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    val config: Flow<AppConfigEntity?> = db.configDao().observeConfig()
    val persons: Flow<List<PersonEntity>> = db.personDao().observeAll()
    val families: Flow<List<PersonEntity>> = db.personDao().observeFamilies()

    suspend fun saveConfig(config: AppConfigEntity) = db.configDao().upsert(config)
    suspend fun currentConfig(): AppConfigEntity? = db.configDao().getConfig()

    suspend fun addPerson(person: PersonEntity) = db.personDao().insert(person)
    suspend fun addPersons(list: List<PersonEntity>) = db.personDao().insertAll(list)
    suspend fun updatePerson(person: PersonEntity) = db.personDao().update(person)
    suspend fun deletePerson(person: PersonEntity) = db.personDao().delete(person)
    suspend fun personById(id: Long) = db.personDao().getById(id)
    suspend fun allPersonsOnce() = db.personDao().getAllOnce()

    suspend fun replaceAll(config: AppConfigEntity?, people: List<PersonEntity>) {
        db.personDao().clear()
        db.configDao().clear()
        config?.let { db.configDao().upsert(it) }
        db.personDao().insertAll(people)
    }
}
