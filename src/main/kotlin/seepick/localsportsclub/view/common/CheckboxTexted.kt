package seepick.localsportsclub.view.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc

private val checkboxRipple = ripple({ Lsc.colors.hoverIndicator })

@Composable
fun CheckboxTexted(
    label: String,
    checked: MutableState<Boolean>,
    enabled: Boolean = true,
    images: Pair<ImageBitmap, ImageBitmap>? = null,
    modifier: Modifier = Modifier,
    textFieldTestTag: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
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
        Text(
            text = label,
            modifier = Modifier.applyTestTag(textFieldTestTag)
        )
        if (images != null) {
            Image(
                bitmap = if (checked.value) images.first else images.second,
                contentDescription = null,
                modifier = Modifier.height(30.dp).padding(start = 10.dp),
                colorFilter = if (enabled) null else ColorFilter.tint(Color.White)
            )
        }
    }
}
