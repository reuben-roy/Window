package com.window.app.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier        = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding  = PaddingValues(vertical = 16.dp)
        ) {

            // ── Accessibility Service ─────────────────────────────────────────
            item {
                PermissionCard(
                    title       = "Accessibility Service",
                    description = "Required to capture foreground app usage and UI content. " +
                            "Tap to open system settings.",
                    isGranted   = state.isAccessibilityEnabled,
                    onAction    = {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    },
                    actionLabel = if (state.isAccessibilityEnabled) "Enabled ✓" else "Enable →"
                )
            }

            // ── Usage Stats ───────────────────────────────────────────────────
            item {
                PermissionCard(
                    title       = "Usage Access",
                    description = "Grants access to UsageStatsManager for ground-truth " +
                            "time verification. Tap to open system settings.",
                    isGranted   = state.isUsageStatsGranted,
                    onAction    = {
                        context.startActivity(
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    },
                    actionLabel = if (state.isUsageStatsGranted) "Granted ✓" else "Grant →"
                )
            }

            // ── Gemini Nano status ────────────────────────────────────────────
            item {
                StatusCard(
                    title  = "Gemini Nano (On-device AI)",
                    status = if (state.isGeminiAvailable)
                        "Available — inference runs entirely on-device 🔒"
                    else
                        "Not available on this device. Requires Pixel 8+ / Android 14+.",
                    isOk   = state.isGeminiAvailable
                )
            }

            // ── Data retention ────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text       = "Data Retention",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = "Events and sessions older than 30 days are automatically deleted nightly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick  = { viewModel.pruneNow() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Prune Now")
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable sub-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onAction: () -> Unit,
    actionLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (isGranted)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = description,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick  = onAction,
                modifier = Modifier.align(Alignment.End),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.error
                )
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, status: String, isOk: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier            = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment   = Alignment.Top
        ) {
            Text(text = if (isOk) "✅" else "⚠️")
            Column {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

