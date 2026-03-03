package com.window.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.window.app.ui.navigation.WindowNavHost
import com.window.app.ui.theme.WindowTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity — single-Activity host for all Compose screens.
 *
 * @AndroidEntryPoint enables Hilt injection in this Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WindowTheme {
                val navController = rememberNavController()
                WindowNavHost(navController = navController)
            }
        }
    }
}

