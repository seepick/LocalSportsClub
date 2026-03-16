package seepick.localsportsclub.view.remark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.model.RemarkRating
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.WidthOrFill

@Composable
fun RemarkView(
    viewModel: RemarkViewModel = koinViewModel(),
) {
    val focusRequester = remember { FocusRequester() }
    var shouldRequestFocus by remember { mutableStateOf(false) }

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus && viewModel.remarks.isNotEmpty()) {
            focusRequester.requestFocus()
            shouldRequestFocus = false
        }
    }
    Column {
        Button(onClick = {
            viewModel.addNewRemark()
            shouldRequestFocus = true
        }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add New")
        }
        Spacer(Modifier.height(5.dp))
        viewModel.remarks.forEachIndexed { index, remark ->
            Row(verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = remark.name,
                    singleLine = true,
                    onValueChange = { remark.name = it },
                    label = { Text("Name") },
                    modifier = Modifier.width(200.dp)
                        .then(if (index == 0) Modifier.focusRequester(focusRequester) else Modifier),
                )
                Spacer(Modifier.width(10.dp))
                DropDownTextField(
                    items = RemarkRating.entries,
                    itemFormatter = { "${it.emoji} ${it.label}" },
                    selectedItem = remark.rating,
                    onItemSelected = { remark.rating = it },
                    label = "Rating",
                    textSize = WidthOrFill.Width(150.dp)
                )

                Spacer(Modifier.width(10.dp))

                OutlinedTextField(
                    value = remark.remark,
                    onValueChange = { remark.remark = it },
                    label = { Text("Remark") },
                    modifier = Modifier.weight(1f),
                )

                Tooltip("Delete") {
                    TextButton(
                        onClick = { viewModel.deleteRemark(remark) },
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
}
