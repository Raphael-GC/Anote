package net.oraphael.anote.data.backup

import android.net.Uri
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.inMemoryDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

// Detecção de permissão perdida e aviso ao usuário.
@RunWith(RobolectricTestRunner::class)
class AutoBackupTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var db: AnoteDatabase
    private lateinit var scope: CoroutineScope
    private lateinit var status: BackupStatusRepository
    private lateinit var autoBackup: AutoBackup

    // Pasta de um provedor qualquer, sem permissão persistida para o app.
    private val folder = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ABackup")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        db = inMemoryDatabase()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        status = BackupStatusRepository(
            PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("status.preferences_pb") }
        )
        autoBackup = AutoBackup(context, BackupRepository(db), BackupFolder(context), status)
    }

    @After
    fun tearDown() {
        db.close()
        scope.cancel()
    }

    @Test
    fun withoutPersistedPermissionReportsPermissionLost() = runTest {
        assertEquals(BackupOutcome.PermissionLost, autoBackup.run(folder))
        assertEquals(BackupProblem.PERMISSION_LOST, status.status.first().problem)
        assertNull(status.status.first().lastBackupAt)
    }

    @Test
    fun withoutFolderClearsTheWarning() = runTest {
        status.recordProblem(BackupProblem.FOLDER_UNAVAILABLE)
        assertEquals(BackupOutcome.NoFolder, autoBackup.run(null))
        assertNull(status.status.first().problem)
    }

    @Test
    fun successfulBackupClearsTheWarning() = runTest {
        status.recordProblem(BackupProblem.PERMISSION_LOST)
        status.recordBackup(Instant.parse("2026-10-12T21:00:00Z"), "anote-backup-2026-10-12-2100.json")
        val current = status.status.first()
        assertNull(current.problem)
        assertEquals("anote-backup-2026-10-12-2100.json", current.lastBackupFile)
    }
}
