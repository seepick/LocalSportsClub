package seepick.localsportsclub.view.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun _UrlTextField() {
    UrlTextField(
        label = "My Website",
        url = "http://www.nu.nl",
        enabled = true,
        onChange = {},
    )
}

@Composable
fun UrlTextField(
    label: String,
    url: String?,
    enabled: Boolean = true,
    onChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    TextField(
        value = url ?: "",
        modifier = modifier,
        label = { Text(label) },
        onValueChange = {
            onChange?.invoke(it)
        },
        singleLine = true,
        textStyle = TextStyle(fontSize = 10.sp),
        enabled = enabled && onChange != null,
        leadingIcon = {
            FilledTonalButton(
                onClick = { uriHandler.openUri(url!!) },
                enabled = enabled && !url.isNullOrEmpty(),
                modifier = Modifier.width(30.dp).height(30.dp),
                contentPadding = PaddingValues.Absolute(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        },
    )
}
