package com.nova.assistant.presentation.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nova.assistant.presentation.ui.theme.*
import com.nova.assistant.util.Constants

/**
 * Horizontally scrollable quick-command suggestion chips shown on an empty chat.
 */
@Composable
fun QuickCommandChips(
    onCommandSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Constants.QUICK_COMMANDS.forEach { cmd ->
            SuggestionChip(
                onClick = { onCommandSelected(cmd) },
                label = {
                    Text(
                        text = cmd,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = BgCard
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = BorderPrimary,
                    borderWidth = 0.5.dp
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
