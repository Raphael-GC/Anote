package net.oraphael.anote.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime

// Arquivo: files/datastore/settings.preferences_pb (excluído do backup do Android).
const val PREFERENCES_NAME = "settings"

val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCES_NAME)

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val preferences: Flow<UserPreferences> = dataStore.data.map { it.toUserPreferences() }

    suspend fun setTheme(theme: AppTheme) = dataStore.edit { it[Keys.THEME] = theme.name }

    suspend fun setDarkMode(mode: DarkMode) = dataStore.edit { it[Keys.DARK_MODE] = mode.name }

    suspend fun setDailyReviewTime(time: LocalTime) =
        dataStore.edit { it[Keys.DAILY_REVIEW_MINUTES] = time.toMinuteOfDay() }

    suspend fun setSundayReviewTime(time: LocalTime) =
        dataStore.edit { it[Keys.SUNDAY_REVIEW_MINUTES] = time.toMinuteOfDay() }

    suspend fun setBackupFolderUri(uri: String?) = dataStore.edit {
        if (uri == null) it.remove(Keys.BACKUP_FOLDER_URI) else it[Keys.BACKUP_FOLDER_URI] = uri
    }

    suspend fun setRemindersEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }

    suspend fun setBackupChoice(choice: BackupChoice) =
        dataStore.edit { it[Keys.BACKUP_CHOICE] = choice.name }

    suspend fun setManualOrderDate(date: LocalDate) =
        dataStore.edit { it[Keys.MANUAL_ORDER_DAY] = date.toEpochDay() }

    // Só conclui depois da resposta obrigatória sobre backup.
    suspend fun completeOnboarding() = dataStore.edit {
        val choice = enumOrDefault(it[Keys.BACKUP_CHOICE], BackupChoice.UNANSWERED)
        check(choice != BackupChoice.UNANSWERED) { "Backup sem resposta" }
        it[Keys.ONBOARDING_COMPLETED] = true
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val DAILY_REVIEW_MINUTES = intPreferencesKey("daily_review_minutes")
        val SUNDAY_REVIEW_MINUTES = intPreferencesKey("sunday_review_minutes")
        val BACKUP_FOLDER_URI = stringPreferencesKey("backup_folder_uri")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val BACKUP_CHOICE = stringPreferencesKey("backup_choice")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val MANUAL_ORDER_DAY = longPreferencesKey("manual_order_day")
    }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val defaults = UserPreferences()
        return UserPreferences(
            theme = enumOrDefault(this[Keys.THEME], defaults.theme),
            darkMode = enumOrDefault(this[Keys.DARK_MODE], defaults.darkMode),
            dailyReviewTime = this[Keys.DAILY_REVIEW_MINUTES]?.toLocalTime() ?: defaults.dailyReviewTime,
            sundayReviewTime = this[Keys.SUNDAY_REVIEW_MINUTES]?.toLocalTime() ?: defaults.sundayReviewTime,
            backupFolderUri = this[Keys.BACKUP_FOLDER_URI],
            remindersEnabled = this[Keys.REMINDERS_ENABLED] ?: defaults.remindersEnabled,
            backupChoice = enumOrDefault(this[Keys.BACKUP_CHOICE], defaults.backupChoice),
            onboardingCompleted = this[Keys.ONBOARDING_COMPLETED] ?: defaults.onboardingCompleted,
            manualOrderDate = this[Keys.MANUAL_ORDER_DAY]?.let(LocalDate::ofEpochDay),
        )
    }
}

private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
    name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default

private fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute

private fun Int.toLocalTime(): LocalTime = LocalTime.of(this / 60, this % 60)
