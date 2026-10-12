package net.oraphael.anote.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.prefs.BackupChoice
import net.oraphael.anote.data.prefs.PreferencesRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AreaRepositoryTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var db: AnoteDatabase
    private lateinit var scope: CoroutineScope
    private lateinit var preferences: PreferencesRepository
    private lateinit var areas: AreaRepository

    @Before
    fun setUp() {
        db = inMemoryDatabase()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        preferences = PreferencesRepository(
            PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("prefs.preferences_pb") }
        )
        areas = AreaRepository(db.areaDao(), preferences)
    }

    @After
    fun tearDown() {
        db.close()
        scope.cancel()
    }

    @Test
    fun startsWithTheDefaultList() = runTest {
        assertEquals(DEFAULT_AREAS, areas.initialNames())
    }

    @Test
    fun onboardingCanEditTheList() = runTest {
        areas.defineAreas(listOf("Trabalho", " Família ", "Lazer"))
        assertEquals(listOf("Trabalho", "Família", "Lazer"), areas.areas.first().map { it.name })
        assertEquals(listOf("Trabalho", "Família", "Lazer"), areas.initialNames())
    }

    @Test
    fun areasAreFixedAfterOnboarding() = runTest {
        areas.defineAreas(listOf("Trabalho"))
        preferences.setBackupChoice(BackupChoice.NO_BACKUP)
        preferences.completeOnboarding()
        assertFailsWith<IllegalStateException> { areas.defineAreas(listOf("Outra")) }
        assertEquals(listOf("Trabalho"), areas.areas.first().map { it.name })
    }

    @Test
    fun validatesNames() {
        assertNull(checkAreaNames(listOf("Casa", "Trabalho")))
        assertEquals(AreaNamesProblem.NONE_LEFT, checkAreaNames(emptyList()))
        assertEquals(AreaNamesProblem.BLANK, checkAreaNames(listOf("Casa", "  ")))
        assertEquals(AreaNamesProblem.DUPLICATE, checkAreaNames(listOf("Casa", " casa")))
    }
}
