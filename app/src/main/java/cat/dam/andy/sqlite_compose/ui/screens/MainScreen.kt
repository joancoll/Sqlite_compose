package cat.dam.andy.sqlite_compose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cat.dam.andy.sqlite_compose.ui.dialogs.DialogState
import cat.dam.andy.sqlite_compose.model.Item
import cat.dam.andy.sqlite_compose.ui.components.ContactList
import cat.dam.andy.sqlite_compose.ui.components.FilterField
import cat.dam.andy.sqlite_compose.ui.dialogs.*
import cat.dam.andy.sqlite_compose.viewmodel.ContactViewModel

@Composable
fun MainScreen(viewModel: ContactViewModel) {
    // Estats per als diàlegs
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

    // Estat per al filtre
    var filter by remember { mutableStateOf("") }

    // Obtenir la llista de contactes des del ViewModel
    val allContacts by viewModel.allContacts.collectAsState()
    val filteredContacts by viewModel.filteredContacts.collectAsState()

    // Actualitzar el filtre al ViewModel quan canvia
    LaunchedEffect(filter) {
        viewModel.setFilter(filter)
    }

    // Interfície d'usuari
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp))
                {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp))
                    {
                        // Camp de filtre
                        FilterField(filter, onFilterChange = { filter = it })

                        // Llista de contactes
                        ContactList(
                            contacts = filteredContacts,
                            onEditClick = { dialogState = DialogState.Edit(it) },
                            onDeleteClick = { dialogState = DialogState.Delete(it) }
                        )
                    }

                    // Botó flotant per afegir contactes
                    FloatingActionButton(
                        onClick = { dialogState = DialogState.Add },
                        modifier = Modifier.align(Alignment.BottomEnd),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    }

                    // Diàlegs
                    when (val currentState = dialogState) {
                        is DialogState.Add -> AddContactDialog(
                            onDismiss = { dialogState = DialogState.None },
                            onConfirm = { name, phone ->
                                viewModel.insert(Item(name = name, tel = phone))
                                dialogState = DialogState.None
                            }
                        )

                        is DialogState.Edit -> EditContactDialog(
                            item = currentState.item,
                            onDismiss = { dialogState = DialogState.None },
                            onConfirm = { id, name, phone ->
                                viewModel.update(Item(id = id, name = name, tel = phone))
                                dialogState = DialogState.None
                            }
                        )

                        is DialogState.Delete -> DeleteContactDialog(
                            item = currentState.item,
                            onDismiss = { dialogState = DialogState.None },
                            onConfirm = { item ->
                                viewModel.delete(item)
                                dialogState = DialogState.None
                            }
                        )

                        is DialogState.None -> {
                            // No es mostra cap diàleg
                        }
                    }
                }
    }
}