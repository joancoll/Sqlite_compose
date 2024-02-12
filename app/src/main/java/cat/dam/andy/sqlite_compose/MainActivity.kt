package cat.dam.andy.sqlite_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cat.dam.andy.sqlite_compose.ui.theme.Sqlite_composeTheme

sealed class DialogState {
    // Controla en quin estat es troba el diàleg (afegir, editar, eliminar o cap)
    object None : DialogState()
    object Add : DialogState()
    data class Edit(val item: Item) : DialogState()
    data class Delete(val item: Item) : DialogState()
}

class MainActivity : ComponentActivity() {
    private var itemList by mutableStateOf<List<Item>>(emptyList())
    private var filter by mutableStateOf("")
    private var dialogState by mutableStateOf<DialogState>(DialogState.None)
    private var databaseHelper = DatabaseHelper(this)
    private var itemToDelete by mutableStateOf<Item?>(null)
    private var itemToEdit by mutableStateOf<Item?>(null)

    @OptIn(ExperimentalComposeUiApi::class)
    private lateinit var keyboardController: SoftwareKeyboardController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sqlite_composeTheme {
                MyApp()
            }
        }
    }

    @Composable
    fun MyApp() {
        // Inicialitzem la llista
        updateResults()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    FilterField()
                    DataView(itemList, onEditClick = { item ->
                        itemToEdit = item
                        dialogState = DialogState.Edit(item)
                    }, onDeleteClick = { item ->
                        itemToDelete = item
                        dialogState = DialogState.Delete(item)
                    })
                }

                FloatingActionButton(
                    onClick = {
                        dialogState = DialogState.Add
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }

                when (val currentState = dialogState) {
                    is DialogState.Add -> ShowAddDialog()
                    is DialogState.Edit -> ShowEditDialog(item = currentState.item)
                    is DialogState.Delete -> ShowDeleteConfirmationDialog(item = currentState.item)
                    is DialogState.None -> {
                        // No mostra cap diàleg
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun FilterField() {
        // Per amagar el teclat virtual quan es tanca el diàleg o s'esborra el filtre
        val localKeyboardController = LocalSoftwareKeyboardController.current
        keyboardController = localKeyboardController ?: return
        // Search Bar
        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            label = { Text("Filter") },
            trailingIcon = {
                IconButton(onClick = {
                    keyboardController.hide()
                    filter = ""
                }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Search"
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                updateResults()
                keyboardController.hide()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // LaunchedEffect per actualitzar automàticament
        LaunchedEffect(filter) {
            updateResults()
        }
    }

    fun updateResults() {
        if (filter.isNotBlank()) {
            itemList = databaseHelper.findContacts(filter)
        } else {
            // Si el camp de cerca està buit, mostrem tots els registres
            itemList = databaseHelper.listAll()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ShowAddDialog() {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }

        Dialog(onDismissRequest = {
            // Tanca el Dialog
            dialogState = DialogState.None
        }) {
            AlertDialog(
                onDismissRequest = { dialogState = DialogState.None },
                title = {
                    Text("Afegir contacte")
                },
                text = {
                    Column {
                        // definim un focusManager dins de l'AlertDialog
                        val focusManager = LocalFocusManager.current
                        TextField(
                            value = name,
                            onValueChange = {
                                name = it
                            },
                            label = { Text("Nom") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    // Mou el focus al següent camp quan es prem "Next" al teclat
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                            },
                            label = { Text("Telèfon") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    createNewItem(name, phone)
                                }
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            createNewItem(name, phone)
                        }
                    ) {
                        Text("Afegir")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            // Tanca el Dialog
                            dialogState = DialogState.None
                        }
                    ) {
                        Text("Cancel·lar")
                    }
                }
            )
        }
    }

    @Composable
    private fun ShowDeleteConfirmationDialog(item: Item) {
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.None },
            title = { Text("Eliminar contacte") },
            text = { Text("Segur que vols eliminar el contacte ${item.name} (${item.tel})?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Implementa la lògica d'eliminació a la base de dades
                        databaseHelper.removeContact(item.id)
                        // Actualitza la llista després de l'eliminació
                        updateResults()
                        dialogState = DialogState.None
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { dialogState = DialogState.None }) {
                    Text("Cancel·lar")
                }
            })
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ShowEditDialog(item: Item) {
        var editedName by remember { mutableStateOf(item.name) }
        var editedPhone by remember { mutableStateOf(item.tel) }

        Dialog(onDismissRequest = {
            // Tanca el Dialog
            dialogState = DialogState.None
        }) {
            AlertDialog(
                onDismissRequest = { dialogState = DialogState.None },
                title = {
                    Text("Editar contacte")
                },
                text = {
                    Column {
                        // definim un focusManager dins de l'AlertDialog
                        val focusManager = LocalFocusManager.current
                        TextField(
                            value = editedName,
                            onValueChange = {
                                editedName = it
                            },
                            label = { Text("Nom") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    // Mou el focus al següent camp quan es prem "Next" al teclat
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = editedPhone,
                            onValueChange = {
                                editedPhone = it
                            },
                            label = { Text("Telèfon") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    updateItem(item.id, editedName, editedPhone)
                                    // Amaga el teclat
                                    focusManager.clearFocus()
                                }
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            updateItem(item.id, editedName, editedPhone)
                            // Tanca el Dialog
                            dialogState = DialogState.None
                        }
                    ) {
                        Text("Guardar canvis")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            // Tanca el Dialog sense desar els canvis
                            dialogState = DialogState.None
                        }
                    ) {
                        Text("Cancel·lar")
                    }
                }
            )
        }
    }

    private fun createNewItem(newName: String, newPhone: String) {
        if (newName.isNotBlank()) {
            val newItem = Item(name = newName, tel = newPhone)
            databaseHelper.addContact(newItem)
            updateResults()
            // Fem desapareixer el diàleg
            dialogState = DialogState.None
        } else {
            Toast.makeText(this, "El nom no pot ser blanc", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateItem(id: Int, editedName: String, editedPhone: String) {
        if (editedName.isNotBlank()) {
            val editedItem = Item(id, editedName, editedPhone)
            databaseHelper.updateContact(editedItem)
            updateResults()
            // Fem desapareixer el diàleg
            dialogState = DialogState.None
        } else {
            Toast.makeText(this, "El nom no pot ser blanc", Toast.LENGTH_SHORT).show()
        }
    }

}