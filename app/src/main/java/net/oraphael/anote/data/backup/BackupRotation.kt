package net.oraphael.anote.data.backup

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val BACKUPS_TO_KEEP = 7
private const val BACKUP_PREFIX = "anote-backup-"
private val nameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")

// O nome ordena cronologicamente: anote-backup-2026-10-12-2105.json
fun backupFileName(at: LocalDateTime): String = "$BACKUP_PREFIX${nameFormatter.format(at)}.json"

// Arquivos de backup automático além dos mais recentes, para apagar. Outros
// arquivos da pasta (exportações manuais, por exemplo) nunca entram na conta.
fun backupsToDelete(fileNames: List<String>, keep: Int = BACKUPS_TO_KEEP): List<String> =
    fileNames
        .filter { it.startsWith(BACKUP_PREFIX) && it.endsWith(".json") }
        .sortedDescending()
        .drop(keep)
