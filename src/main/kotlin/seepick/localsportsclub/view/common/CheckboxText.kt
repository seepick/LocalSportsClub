package seepick.localsportsclub.view.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun CheckboxText(
    label: String,
    enabled: Boolean,
    isTrue: MutableState<Boolean>,
    images: Pair<ImageBitmap, ImageBitmap>? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isTrue.value,
            onCheckedChange = { isTrue.value = it },
            enabled = enabled,
        )
        Text(label)
        if (images != null) {
            Image(
                bitmap = if (isTrue.value) images.first else images.second,
                contentDescription = null,
                modifier = Modifier.height(30.dp).padding(start = 10.dp),
                colorFilter = if (enabled) null else ColorFilter.tint(Color.White)
            )
        }
    }
}
