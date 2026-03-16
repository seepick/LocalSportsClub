package seepick.localsportsclub.view.remark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.WidthOrFill

@Composable
fun RemarkView(
    viewModel: RemarkViewModel = koinViewModel(),
) {
    Column {
        TextButton(onClick = { viewModel.addNewRemark() }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add New", color = Lsc.colors.primary)
        }
        Spacer(Modifier.height(5.dp))
        viewModel.remarks.forEach { remark ->
            Row(verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = remark.name,
                    singleLine = true,
                    onValueChange = { remark.name = it },
                    label = { Text("Name") },
                    modifier = Modifier.width(200.dp),
                )
                Spacer(Modifier.width(10.dp))
                val realRating = remark.rating
                if (realRating is RemarkRating.Activity) {
                    DropDownTextField(
                        items = realRating.items,
                        itemFormatter = { "${it.emoji} ${it.label}" },
                        selectedItem = realRating.rating,
                        onItemSelected = { remark.rating = RemarkRating.Activity(it) },
                        label = "Rating",
                        textSize = WidthOrFill.Width(150.dp)
                    )
                } else if (realRating is RemarkRating.Teacher) {
                    DropDownTextField(
                        items = realRating.items,
                        itemFormatter = { "${it.emoji} ${it.label}" },
                        selectedItem = realRating.rating,
                        onItemSelected = { remark.rating = RemarkRating.Teacher(it) },
                        label = "Rating",
                        textSize = WidthOrFill.Width(150.dp)
                    )
                } else error("unhandled remark type ${remark.rating::class.simpleName}")

                Spacer(Modifier.width(10.dp))

                OutlinedTextField(
                    value = remark.remark,
                    onValueChange = { remark.remark = it },
                    label = { Text("Remark") },
                    modifier = Modifier.weight(1f),
                )

                Tooltip("Delete this remark") {
                    TextButton(onClick = { viewModel.deleteRemark(remark) }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
}
