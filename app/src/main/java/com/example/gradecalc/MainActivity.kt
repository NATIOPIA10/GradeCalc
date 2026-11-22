package com.example.gradecalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gradecalc.ui.AppNavigation
import com.example.gradecalc.ui.GradeViewModel
import com.example.gradecalc.ui.WithLanguage
import com.example.gradecalc.ui.theme.GradeCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: GradeViewModel = viewModel()
            val language by vm.language.collectAsState()
            WithLanguage(language) {
                GradeCalcTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        AppNavigation(vm)
                    }
                }
            }
        }
    }
}
