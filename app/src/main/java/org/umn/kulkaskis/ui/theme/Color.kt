package org.umn.kulkaskis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary: Color          @Composable get() = MaterialTheme.colorScheme.primary
val PrimaryContainer: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
val Accent: Color           @Composable get() = MaterialTheme.colorScheme.tertiary
val InkDark: Color          @Composable get() = MaterialTheme.colorScheme.onTertiary
val Background: Color       @Composable get() = MaterialTheme.colorScheme.background
val Ink: Color              @Composable get() = MaterialTheme.colorScheme.onBackground
val InkMuted: Color         @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val Border: Color           @Composable get() = MaterialTheme.colorScheme.outline
val ErrorRed: Color         @Composable get() = MaterialTheme.colorScheme.error