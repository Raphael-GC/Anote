package net.oraphael.anote.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.oraphael.anote.data.assertFailsWith
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalTime

class PreferencesRepositoryTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var preferences: PreferencesRepository

    @Before
    fun setUp() {
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        preferences = PreferencesRepository(
            PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("prefs.preferences_pb") }
        )
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun defaultsMatchThePlan() = runTest {
        val prefs = preferences.preferences.first()
        assertEquals(LocalTime.of(21, 0), prefs.dailyReviewTime)
        assertEquals(LocalTime.of(19, 0), prefs.sundayReviewTime)
        assertEquals(BackupChoice.UNANSWERED, prefs.backupChoice)
        assertFalse(prefs.onboardingCompleted)
    }

    @Test
    fun onboardingCannotCompleteWithoutBackupAnswer() = runTest {
        assertFailsWith<IllegalStateException> { preferences.completeOnboarding() }
        assertFalse(preferences.preferences.first().onboardingCompleted)
    }

    @Test
    fun negativeBackupAnswerIsEnoughToComplete() = runTest {
        preferences.setBackupChoice(BackupChoice.NO_BACKUP)
        preferences.completeOnboarding()
        assertTrue(preferences.preferences.first().onboardingCompleted)
    }

    @Test
    fun choosingAFolderIsEnoughToComplete() = runTest {
        preferences.setBackupChoice(BackupChoice.FOLDER)
        preferences.completeOnboarding()
        assertTrue(preferences.preferences.first().onboardingCompleted)
    }
}
