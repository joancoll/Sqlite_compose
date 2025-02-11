package cat.dam.andy.sqlite_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cat.dam.andy.sqlite_compose.data.DatabaseHelper
import cat.dam.andy.sqlite_compose.ui.screens.MainScreen
import cat.dam.andy.sqlite_compose.viewmodel.ContactViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val databaseHelper = DatabaseHelper(this)
        val viewModel = ContactViewModel(databaseHelper)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}