package seepick.localsportsclub.view.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc

private val checkboxRipple = ripple({ Lsc.colors.hoverIndicator })

@Composable
fun CheckboxTexted(
    checked: MutableState<Boolean>,
    label: String? = null,
    enabled: Boolean = true,
    icon: VisualIndicator? = null,
    modifier: Modifier = Modifier,
    textFieldTestTag: String? = null,
    tooltipText: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Tooltip(tooltipText) {
        Row(
            Modifier
                .toggleable(
                    enabled = enabled,
                    value = checked.value,
                    interactionSource = interactionSource,
                    indication = checkboxRipple,
                    onValueChange = { checked.value = it },
                    role = Role.Checkbox,
                )
                .padding(4.dp) // must be after toggleable, to pad within the box
                .then(modifier),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked.value,
                onCheckedChange = null, // null recommended for accessibility with screenreaders
            )
            Spacer(Modifier.width(4.dp))
            if (label != null) {
                Text(
                    text = label,
                    modifier = Modifier.applyTestTag(textFieldTestTag)
                )
            }
            icon?.composeIt(alpha = if (checked.value) 1.0f else 0.5f, paddingEnd = null)
        }
    }
}
