package seepick.localsportsclub.view.remark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RemarkView(
    height: Int,
    remarks: List<RemarkViewEntity>,
    nameSuggestions: List<String>,
    onAddNewClicked: () -> Unit,
    onDelete: (RemarkViewEntity) -> Unit,
) {
//    val focusRequester = remember { FocusRequester() }
//    var shouldRequestFocus by remember { mutableStateOf(false) }

//    LaunchedEffect(shouldRequestFocus) {
//        if (shouldRequestFocus && viewModel.remarks.isNotEmpty()) {
//            focusRequester.requestFocus()
//            shouldRequestFocus = false
//        }
//    }
    Column {
        Button(
            onClick = onAddNewClicked  // FIXME viewModel.addNewRemark()
//            shouldRequestFocus = true
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add New")
        }
        Spacer(Modifier.height(5.dp))
        val roughlyConsumedHeight = 55
        RemarkTable(
            remarks = remarks,
            nameSuggestions = nameSuggestions,
            onDelete = onDelete,
            boxModifier = Modifier.height((height - roughlyConsumedHeight).dp),
        )
    }
}
