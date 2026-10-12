package net.oraphael.anote.ui.theme

import net.oraphael.anote.data.prefs.AppTheme

fun tokensFor(theme: AppTheme): ThemeTokens = when (theme) {
    AppTheme.AUTUMN_NOTEBOOK -> AutumnNotebookTokens
    AppTheme.NIGHT_COZY -> NightCozyTokens
    AppTheme.POCKET_GARDEN -> PocketGardenTokens
}
