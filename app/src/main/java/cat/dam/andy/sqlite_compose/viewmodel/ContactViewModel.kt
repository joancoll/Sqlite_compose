package cat.dam.andy.sqlite_compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.dam.andy.sqlite_compose.data.DatabaseHelper
import cat.dam.andy.sqlite_compose.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactViewModel(private val databaseHelper: DatabaseHelper) : ViewModel() {

    private val _allContacts = MutableStateFlow<List<Item>>(emptyList())
    val allContacts: StateFlow<List<Item>> get() = _allContacts

    private val _filteredContacts = MutableStateFlow<List<Item>>(emptyList())
    val filteredContacts: StateFlow<List<Item>> get() = _filteredContacts

    private var currentFilter = ""

    init {
        // Inicialitzem la llista de contactes
        viewModelScope.launch {
            _allContacts.value = databaseHelper.listAll()
            _filteredContacts.value = _allContacts.value
        }
    }

    fun setFilter(filter: String) {
        currentFilter = filter
        updateFilteredContacts()
    }

    private fun updateFilteredContacts() {
        _filteredContacts.value = if (currentFilter.isNotBlank()) {
            databaseHelper.findContacts(currentFilter)
        } else {
            _allContacts.value
        }
    }

    fun insert(item: Item) = viewModelScope.launch {
        databaseHelper.addContact(item)
        _allContacts.value = databaseHelper.listAll()
        updateFilteredContacts() // Actualitzem la llista filtrada
    }

    fun update(item: Item) = viewModelScope.launch {
        databaseHelper.updateContact(item)
        _allContacts.value = databaseHelper.listAll()
        updateFilteredContacts() // Actualitzem la llista filtrada
    }

    fun delete(item: Item) = viewModelScope.launch {
        databaseHelper.removeContact(item.id)
        _allContacts.value = databaseHelper.listAll()
        updateFilteredContacts() // Actualitzem la llista filtrada
    }
}