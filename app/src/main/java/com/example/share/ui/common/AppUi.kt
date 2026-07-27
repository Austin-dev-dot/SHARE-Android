package com.example.share.ui.common

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.share.theme.BorderSoft
import com.example.share.theme.Evergreen
import com.example.share.theme.EvergreenDeep
import com.example.share.theme.MintWash
import com.example.share.theme.Sand
import com.example.share.theme.SkyWash

private const val SupportWhatsAppNumber = "919354627100"

@Composable
fun rememberWhatsAppSupportAction(source: String): () -> Unit {
    val context = LocalContext.current
    return remember(context, source) {
        {
            val message = Uri.encode("Hi SHARE team, I need help with $source.")
            val uri = Uri.parse("https://wa.me/$SupportWhatsAppNumber?text=$message")
            val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(intent) }.onFailure {
                Toast.makeText(context, "Unable to open WhatsApp support right now.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun ShareBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(Sand, Color.White, SkyWash.copy(alpha = 0.34f))
            )
        ),
        content = content
    )
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
fun HeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, Evergreen.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color.White, MintWash, SkyWash.copy(alpha = 0.65f))
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                eyebrow?.let {
                    Text(
                        text = it.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Evergreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = EvergreenDeep
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                content()
            }
        )
    }
}

@Composable
fun SectionHeading(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.74f), RoundedCornerShape(20.dp))
            .border(1.dp, BorderSoft, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = EvergreenDeep
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SelectablePill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val background by animateColorAsState(
        targetValue = if (selected) Evergreen else Color.White,
        label = "pill-bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        label = "pill-text"
    )

    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(20.dp))
            .border(1.dp, if (selected) Evergreen else BorderSoft, RoundedCornerShape(20.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
fun PrimaryAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Evergreen,
            contentColor = Color.White,
            disabledContainerColor = Evergreen.copy(alpha = 0.45f)
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun SecondaryAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = EvergreenDeep,
            disabledContentColor = EvergreenDeep.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun InlineInfoBanner(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Evergreen,
    backgroundColor: Color = MintWash
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(22.dp))
            .border(1.dp, accentColor.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(accentColor, CircleShape)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = accentColor
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DetailLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SupportStrip(
    source: String,
    modifier: Modifier = Modifier
) {
    val openSupport = rememberWhatsAppSupportAction(source)
    SectionCard(modifier = modifier) {
        Text(
            text = "Need a real person?",
            style = MaterialTheme.typography.titleMedium,
            color = EvergreenDeep
        )
        Text(
            text = "Chat with SHARE support on WhatsApp for pickup help, campaign questions, or volunteer guidance.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SecondaryAppButton(
            text = "Open WhatsApp support",
            onClick = openSupport,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WhatsAppSupportFab(
    source: String,
    modifier: Modifier = Modifier
) {
    val openSupport = rememberWhatsAppSupportAction(source)
    FloatingActionButton(
        onClick = openSupport,
        modifier = modifier,
        containerColor = Evergreen,
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = "WhatsApp support"
        )
    }
}

@Composable
fun BalancedRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}
