package cat.dam.andy.sqlite_compose.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import cat.dam.andy.sqlite_compose.model.Item

@Composable
fun DeleteContactDialog(
    item: Item,
    onDismiss: () -> Unit,
    onConfirm: (item: Item) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Eliminar contacte") },
            text = { Text("Segur que vols eliminar el contacte ${item.name} (${item.tel})?") },
            confirmButton = {
                Button(onClick = { onConfirm(item) }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel·lar")
                }
            }
        )
    }
}