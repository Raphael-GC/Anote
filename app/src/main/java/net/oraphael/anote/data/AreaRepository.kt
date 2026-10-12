package net.oraphael.anote.data

import kotlinx.coroutines.flow.Flow
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.AreaDao

// Lista inicial de áreas. Editável apenas no onboarding; depois dele, fixa.
val DEFAULT_AREAS = listOf("Trabalho", "Casa", "Saúde", "Finanças", "Estudos", "Pessoal")

class AreaRepository(private val areaDao: AreaDao) {
    val areas: Flow<List<Area>> = areaDao.observeAll()

    // Grava a lista padrão só quando ainda não há nenhuma área.
    suspend fun ensureDefaults() {
        if (areaDao.count() == 0) {
            areaDao.insertAll(DEFAULT_AREAS.mapIndexed { i, name -> Area(name = name, sortOrder = i) })
        }
    }
}
