package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.astro.ui.AstroMainScreen
import com.example.astro.ui.AstroViewModel
import com.example.astro.ui.theme.AstroAppTheme

class MainActivity : ComponentActivity() {

    private val astroViewModel: AstroViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isRedMode by astroViewModel.isRedNightMode.collectAsState()

            AstroAppTheme(isRedNightMode = isRedMode) {
                AstroMainScreen(viewModel = astroViewModel)
            }
        }
    }
}
