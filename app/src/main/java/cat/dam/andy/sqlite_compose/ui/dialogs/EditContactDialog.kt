package cat.dam.andy.sqlite_compose.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cat.dam.andy.sqlite_compose.model.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactDialog(
    item: Item,
    onDismiss: () -> Unit,
    onConfirm: (id: Int, name: String, phone: String) -> Unit
) {
    var editedName by remember { mutableStateOf(item.name) }
    var editedPhone by remember { mutableStateOf(item.tel) }

    Dialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Editar contacte") },
            text = {
                Column {
                    val focusManager = LocalFocusManager.current
                    TextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Nom") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        })
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editedPhone,
                        onValueChange = { editedPhone = it },
                        label = { Text("Telèfon") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onConfirm(item.id, editedName, editedPhone)
                        })
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onConfirm(item.id, editedName, editedPhone) }) {
                    Text("Guardar canvis")
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