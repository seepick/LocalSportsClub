package seepick.localsportsclub.view.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.view.common.Cancel
import seepick.localsportsclub.view.common.LscHScroll
import seepick.localsportsclub.view.common.Tooltip

@Composable
fun GenericSearchPanel(
    clearSearchEnabled: Boolean,
    clearSearch: () -> Unit,
    content: @Composable () -> Unit,
) {
    val state = rememberScrollState()
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.horizontalScroll(state)
                .padding(bottom = 10.dp) // gap for the scrollbar
        ) {
            if (clearSearchEnabled) {
                Tooltip("Reset search criteria") {
                    TextButton(
                        onClick = clearSearch,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .defaultMinSize(20.dp, 20.dp),
                    ) {
                        Icon(
                            Icons.Filled.Cancel, contentDescription = null,
                        )
                    }
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
            content()
        }
        LscHScroll(rememberScrollbarAdapter(state))
    }
}
