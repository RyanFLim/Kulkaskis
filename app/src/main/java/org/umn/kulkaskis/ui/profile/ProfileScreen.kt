package org.umn.kulkaskis.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.umn.kulkaskis.data.FridgeItem
import org.umn.kulkaskis.data.sampleItems
import org.umn.kulkaskis.notify.ExpiryScheduler
import org.umn.kulkaskis.ui.home.kFieldColors
import org.umn.kulkaskis.ui.theme.Kulkaskis
import org.umn.kulkaskis.ui.theme.KulkaskisTheme

@Composable
fun ProfileScreen(
    userName: String = "Ryan",
    email: String = "",
    items: List<FridgeItem> = sampleItems,
    onSaveName: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    appearance: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var draft by remember(userName) { mutableStateOf(userName) }
    val changed = draft.isNotBlank() && draft.trim() != userName

    val total = items.size
    val expiringSoon = items.count { it.isExpiringSoon }
    val expired = items.count { it.daysLeft < 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Avatar with initial, name and email centered
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(scheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    userName.trim().firstOrNull()?.uppercase() ?: "?",
                    color = scheme.onPrimaryContainer,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(userName, style = MaterialTheme.typography.titleMedium)
            if (email.isNotBlank()) {
                Text(
                    email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your name") },
            singleLine = true,
            isError = draft.isBlank(),
            supportingText = { if (draft.isBlank()) Text("Name is required") },
            shape = RoundedCornerShape(10.dp),
            colors = kFieldColors()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onSaveName(draft) },
            enabled = changed,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save name", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(28.dp))
        Text("Fridge summary", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard("Total", total.toString(), scheme.onSurface)
            StatCard("Expiring", expiringSoon.toString(), Kulkaskis.status.amberFg)
            StatCard("Expired", expired.toString(), scheme.error)
        }

        Spacer(Modifier.height(28.dp))
        appearance()

        Spacer(Modifier.height(28.dp))
        Text("Reminders", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, scheme.outline)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "You get a daily reminder at 08:00 about items that are expiring soon.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { ExpiryScheduler.runNow(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primaryContainer,
                        contentColor = scheme.onPrimaryContainer
                    )
                ) {
                    Text("Send test reminder", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(36.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, scheme.error),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Log out", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Kulkaskis v1.0",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RowScope.StatCard(label: String, value: String, valueColor: Color) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, scheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                color = valueColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    KulkaskisTheme {
        ProfileScreen(userName = "Ryan", email = "ryan@mail.com")
    }
}