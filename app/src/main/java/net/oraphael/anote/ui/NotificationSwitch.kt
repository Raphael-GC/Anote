package net.oraphael.anote.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import net.oraphael.anote.R
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing

private fun Context.hasNotificationPermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

// Interruptor de uma função que notifica. A permissão do sistema só é pedida
// quando a pessoa liga a função, nunca antes.
@Composable
fun NotificationSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val colors = LocalColorTokens.current
    var granted by remember { mutableStateOf(context.hasNotificationPermission()) }
    var denied by remember { mutableStateOf(false) }
    // A permissão pode mudar nas configurações do sistema enquanto o app está aberto.
    LifecycleResumeEffect(Unit) {
        granted = context.hasNotificationPermission()
        onPauseOrDispose { }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        denied = !ok
        if (ok) onCheckedChange(true)
    }

    fun toggle(on: Boolean) {
        when {
            !on -> onCheckedChange(false)
            granted -> onCheckedChange(true)
            else -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Switch, onValueChange = ::toggle)
                .padding(vertical = Spacing.s),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = null, modifier = Modifier.padding(start = Spacing.m))
        }
        // Ligado sem permissão (negada agora ou revogada depois): explica e leva às configurações.
        if ((checked && !granted) || denied) {
            Text(
                text = stringResource(R.string.notifications_blocked),
                style = MaterialTheme.typography.bodySmall,
                color = colors.overdue,
            )
            TextButton(onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                )
            }) { Text(stringResource(R.string.notifications_open_settings)) }
        }
    }
}
