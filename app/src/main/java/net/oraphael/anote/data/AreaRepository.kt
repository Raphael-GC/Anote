package net.oraphael.anote.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.AreaDao
import net.oraphael.anote.data.prefs.PreferencesRepository

// Lista inicial de áreas. Editável apenas no onboarding; depois dele, fixa.
val DEFAULT_AREAS = listOf("Trabalho", "Casa", "Saúde", "Finanças", "Estudos", "Pessoal")

const val MAX_AREA_NAME_LENGTH = 30

enum class AreaNamesProblem { NONE_LEFT, BLANK, DUPLICATE }

fun checkAreaNames(names: List<String>): AreaNamesProblem? = when {
    names.isEmpty() -> AreaNamesProblem.NONE_LEFT
    names.any { it.isBlank() } -> AreaNamesProblem.BLANK
    names.map { it.trim().lowercase() }.toSet().size < names.size -> AreaNamesProblem.DUPLICATE
    else -> null
}

class AreaRepository(
    private val areaDao: AreaDao,
    private val preferencesRepository: PreferencesRepository,
) {
    val areas: Flow<List<Area>> = areaDao.observeAll()

    // Áreas salvas ou, se ainda não houver nenhuma, a lista padrão.
    suspend fun initialNames(): List<String> =
        areaDao.getAll().map { it.name }.ifEmpty { DEFAULT_AREAS }

    // Só durante o onboarding: depois dele, as áreas ficam fixas.
    suspend fun defineAreas(names: List<String>) {
        check(!preferencesRepository.preferences.first().onboardingCompleted) { "Áreas são fixas após o onboarding" }
        require(checkAreaNames(names) == null) { "Nomes de áreas inválidos" }
        areaDao.replaceAll(names.mapIndexed { i, name -> Area(name = name.trim(), sortOrder = i) })
    }
}
