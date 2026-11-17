package org.umn.kulkaskis.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.umn.kulkaskis.ui.theme.Kulkaskis
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class BadgeKind { OK, WARN, BAD }
fun daysLeft(expiry: LocalDate): Long =
    ChronoUnit.DAYS.between(LocalDate.now(), expiry)
fun statusFor(expiry: LocalDate): Pair<String, BadgeKind> {
    val d = daysLeft(expiry)
    return when {
        d < 0 -> "Expired" to BadgeKind.BAD
        d <= 3 -> "${d}d left" to BadgeKind.WARN
        else -> "Fresh" to BadgeKind.OK
    }
}

fun expiringCount(expiries: List<LocalDate>): Int =
    expiries.count { daysLeft(it) in 0..3 }

@Composable
fun StatusBadge(label: String, kind: BadgeKind) {
    val st = Kulkaskis.status
    val bg: Color
    val fg: Color
    when (kind) {
        BadgeKind.OK -> { bg = st.greenBg; fg = st.greenFg }
        BadgeKind.WARN -> { bg = st.amberBg; fg = st.amberFg }
        BadgeKind.BAD -> { bg = st.redBg; fg = st.redFg }
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ExpiryBadge(expiry: LocalDate) {
    val (label, kind) = statusFor(expiry)
    StatusBadge(label, kind)
}

@Composable
fun ExpiringBanner(expiring: Int, expired: Int, modifier: Modifier = Modifier) {
    // The banner always reflects the real state of the fridge.
    val st = Kulkaskis.status
    val (title, subtitle, bg) = when {
        expired > 0 && expiring > 0 -> Triple(
            "$expiring expiring soon, $expired expired",
            "Use the soon-to-expire items first and remove the expired ones",
            st.bannerOkBg
        )
        expired > 0 -> Triple(
            if (expired == 1) "1 item has expired" else "$expired items have expired",
            "Remove them from your fridge to keep it up to date",
            st.bannerBadBg
        )
        expiring > 0 -> Triple(
            if (expiring == 1) "1 item expiring soon" else "$expiring items expiring soon",
            "Use them first or check the Recipes tab",
            st.bannerOkBg
        )
        else -> Triple(
            "Everything is fresh",
            "Nothing expires in the next 3 days",
            st.bannerOkBg
        )
    }
    val onBanner = st.bannerText
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                title,
                color = onBanner,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                color = onBanner.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun ScanFridgeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth().height(50.dp)
    ) { Text("Scan Fridge", fontWeight = FontWeight.SemiBold) }
}

@Composable
fun KChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun kFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline
)