package net.oraphael.anote.data.prefs

import java.time.LocalDate
import java.time.LocalTime

// Caderno de Outono, Aconchego Noturno e Jardim de Bolso.
enum class AppTheme { AUTUMN_NOTEBOOK, NIGHT_COZY, POCKET_GARDEN }

enum class DarkMode { SYSTEM, LIGHT, DARK }

// Resposta obrigatória do onboarding sobre backup.
enum class BackupChoice { UNANSWERED, FOLDER, NO_BACKUP }

// Preferências da seção 5.
data class UserPreferences(
    val theme: AppTheme = AppTheme.AUTUMN_NOTEBOOK,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val dailyReviewTime: LocalTime = LocalTime.of(21, 0),
    val sundayReviewTime: LocalTime = LocalTime.of(19, 0),
    val backupFolderUri: String? = null,
    // Lembretes das revisões e do backup: ligados por padrão; a pessoa desliga se quiser.
    val remindersEnabled: Boolean = true,
    // Aviso de prazo: opcional, desligado por padrão.
    val dueNoticeEnabled: Boolean = false,
    val backupChoice: BackupChoice = BackupChoice.UNANSWERED,
    val onboardingCompleted: Boolean = false,
    // Dia em que a ordem manual foi feita na revisão.
    val manualOrderDate: LocalDate? = null,
)
