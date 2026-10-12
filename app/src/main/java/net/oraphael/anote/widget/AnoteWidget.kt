package net.oraphael.anote.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.flow.first
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.MainActivity
import net.oraphael.anote.R
import net.oraphael.anote.data.SortOrder
import net.oraphael.anote.data.prefs.AppTheme
import net.oraphael.anote.data.reminders.ReviewReminders
import net.oraphael.anote.ui.theme.ColorTokens
import net.oraphael.anote.ui.theme.tokensFor
import java.time.LocalDate

private const val MAX_FOCUSES = 5

// Widget: focos de hoje e atalho para a captura rápida. Tipografia simplificada:
// uma família por tema (serifa no Caderno de Outono) e só dois tamanhos.
class AnoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as AnoteApp
        val prefs = app.preferencesRepository.preferences.first()
        val focuses = app.mainListSource.observe(LocalDate.now(), SortOrder.OLDEST)
            .first().focuses.map { it.entry.text }
        val tokens = tokensFor(prefs.theme)
        val family = if (prefs.theme == AppTheme.AUTUMN_NOTEBOOK) FontFamily.Serif else FontFamily.SansSerif
        provideContent {
            WidgetContent(context, focuses, tokens.light, tokens.dark, family)
        }
    }
}

// Cor do token no tema claro e no escuro.
private fun themed(light: ColorTokens, dark: ColorTokens, pick: (ColorTokens) -> Color) =
    ColorProvider(day = pick(light), night = pick(dark))

@Composable
private fun WidgetContent(
    context: Context,
    focuses: List<String>,
    light: ColorTokens,
    dark: ColorTokens,
    family: FontFamily,
) {
    val ink = themed(light, dark) { it.onSurface }
    val muted = themed(light, dark) { it.onSurfaceVariant }
    val captureIntent = Intent(context, MainActivity::class.java)
        .putExtra(ReviewReminders.EXTRA_OPEN, OPEN_CAPTURE)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val openApp = Intent(context, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(themed(light, dark) { it.background })
            .cornerRadius(16.dp)
            .padding(12.dp),
    ) {
        Text(
            text = context.getString(R.string.list_focuses_title),
            style = TextStyle(color = themed(light, dark) { it.primary }, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = family),
            modifier = GlanceModifier.clickable(actionStartActivity(openApp)),
        )
        Spacer(GlanceModifier.height(6.dp))
        Column(GlanceModifier.defaultWeight().fillMaxWidth().clickable(actionStartActivity(openApp))) {
            if (focuses.isEmpty()) {
                Text(
                    text = context.getString(R.string.widget_no_focus),
                    style = TextStyle(color = muted, fontSize = 14.sp, fontFamily = family),
                )
            } else {
                focuses.take(MAX_FOCUSES).forEach { text ->
                    Text(
                        text = "• $text",
                        maxLines = 1,
                        style = TextStyle(color = ink, fontSize = 14.sp, fontFamily = family),
                        modifier = GlanceModifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
        Row(GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            Text(
                text = "+ " + context.getString(R.string.capture_open),
                style = TextStyle(color = themed(light, dark) { it.onPrimary }, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = family),
                modifier = GlanceModifier
                    .background(themed(light, dark) { it.primary })
                    .cornerRadius(12.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable(actionStartActivity(captureIntent)),
            )
        }
    }
}

const val OPEN_CAPTURE = "capture"

class AnoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AnoteWidget()
}
