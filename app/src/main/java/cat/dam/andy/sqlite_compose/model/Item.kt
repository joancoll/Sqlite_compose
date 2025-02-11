package cat.dam.andy.sqlite_compose.model

data class Item(
    val id: Int = -1, // Valor per defecte per a que no sigui obligatori (autoincremental)
    val name: String,
    val tel: String
)