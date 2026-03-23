package seepick.localsportsclub.view.common

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import seepick.localsportsclub.LocalTextFieldColors
import seepick.localsportsclub.Lsc

@Composable
fun PasswordField(password: String, onChange: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = password,
        onValueChange = onChange,
        label = { Text("Password") },
        singleLine = true,
        colors = LocalTextFieldColors.current,
        placeholder = { Text("Password") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = Lsc.icons.passwordVisibility(passwordVisible),
                    contentDescription = null,
                    tint = Lsc.colors.clickableNeutral,
                )
            }
        })
}
