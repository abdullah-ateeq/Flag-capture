package com.example.flagcapture.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flagcapture.ui.theme.FlagGold
import com.example.flagcapture.ui.theme.FlagGoldBright
import com.example.flagcapture.ui.theme.LegalCaptureIndicator
import com.example.flagcapture.ui.theme.Player1Light
import com.example.flagcapture.ui.theme.Player2Light

@Composable
fun HowToPlayScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "TACTICAL BRIEFING",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Field manual & engagement rules",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Rule Card 1: Objective
            RuleSectionCard(
                icon = Icons.Default.EmojiEvents,
                iconTint = FlagGoldBright,
                title = "Primary Objective",
                content = "Two commanders battle on an 8×8 tactical grid. Move soldiers across the field to infiltrate enemy lines and capture the opponent's hidden flag, or eliminate their entire combat force."
            )

            // Rule Card 2: Units
            RuleSectionCard(
                icon = Icons.Default.Shield,
                iconTint = Player1Light,
                title = "Pieces & Movement",
                content = "Each player controls 6 pieces:\n" +
                        "• 5 Soldiers: Move one cell orthogonally (Up, Down, Left, Right). No diagonal moves.\n" +
                        "• 1 Flag: Completely fixed. Flags cannot move and remain anchored at their starting base."
            )

            // Rule Card 3: Hidden Information
            RuleSectionCard(
                icon = Icons.Default.VisibilityOff,
                iconTint = Player2Light,
                title = "Hidden Fog of War",
                content = "Opponent pieces appear as unknown tactical markers until engagement occurs. You always know your own pieces and your flag location. Deceive your enemy by keeping your flag well-guarded without revealing its identity."
            )

            // Rule Card 4: Combat Resolution
            RuleSectionCard(
                icon = Icons.Default.Close,
                iconTint = LegalCaptureIndicator,
                title = "Combat & Elimination",
                content = "When a soldier advances onto an enemy piece, combat is resolved immediately:\n" +
                        "• Soldier vs Soldier: Mutual destruction! Both pieces are removed from the board.\n" +
                        "• Soldier vs Flag: Immediate victory! The enemy flag is uncovered and the attacker wins."
            )

            // Rule Card 5: Terrain & Obstacles
            RuleSectionCard(
                icon = Icons.Default.Block,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "Stone Obstacles",
                content = "6 impassable stone obstacles are positioned in the neutral central sector. No piece may enter or jump across blocked cells. Use them as tactical chokepoints and defensive shields."
            )

            // Rule Card 6: Strategy & Tips
            RuleSectionCard(
                icon = Icons.Default.Lightbulb,
                iconTint = FlagGold,
                title = "Tactical Advice",
                content = "• Don't rush recklessly—mutual elimination means every soldier trade is critical.\n" +
                        "• Feign flag protection in one sector while masking the true objective.\n" +
                        "• Use stone obstacles to guard your flank against enemy incursions."
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RuleSectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(0.8.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
