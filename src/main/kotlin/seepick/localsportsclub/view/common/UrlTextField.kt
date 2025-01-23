package seepick.localsportsclub.view.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.Lsc

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
fun UrlText(url: String, displayText: String = url) {
    val uriHandler = LocalUriHandler.current
    ClickableText(text = displayText, onClick = { uriHandler.openUri(url) })
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
fun ClickableText(
    text: String,
    notHoveredColor: Color = Lsc.colors.primary,
    onClick: () -> Unit,
) {
    var isHovered by remember { mutableStateOf(false) }
    val color = if (!isHovered) notHoveredColor else Lsc.colors.primaryBrighter
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = color,
        modifier = Modifier
//            .padding(top = 3.dp, bottom = 3.dp)
            .onClick(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
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
            LinkTonalButton(url, enabled = enabled, useHandIcon = true)
        },
    )
}

@Composable
fun LinkTonalButton(
    url: String?,
    enabled: Boolean = true,
    useHandIcon: Boolean = false,
) {
    val uriHandler = LocalUriHandler.current
    val ultimatelyEnabled = enabled && !url.isNullOrEmpty()
    FilledTonalButton(
        onClick = { uriHandler.openUri(url!!) },
        enabled = ultimatelyEnabled,
        modifier = Modifier.width(30.dp).height(30.dp).let {
            if (useHandIcon && ultimatelyEnabled) it.pointerHoverIcon(PointerIcon.Hand) else it
        },
        contentPadding = PaddingValues.Absolute(),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
        )
    }
}
