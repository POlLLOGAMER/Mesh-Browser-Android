package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.database.MeshDatabase
import com.example.data.repository.MeshRepository
import com.example.ui.screens.MeshBrowserScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MeshViewModel
import com.example.ui.viewmodel.MeshViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize offline local Room database container
        val database = MeshDatabase.getDatabase(this)
        
        // 2. Initialize MVVM repository & factories
        val repository = MeshRepository(database.meshDao())
        val viewModelFactory = MeshViewModelFactory(repository)
        
        // 3. Delegate viewmodel initialization
        val viewModel: MeshViewModel by viewModels { viewModelFactory }

        // 4. Enable full edge-to-edge screen support
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MeshBrowserScreen(viewModel = viewModel)
                }
            }
        }
    }
}
