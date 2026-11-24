package com.example.gradecalc

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gradecalc.ui.AppNavigation
import com.example.gradecalc.ui.GradeViewModel
import com.example.gradecalc.ui.LockScreen
import com.example.gradecalc.ui.WithLanguage
import com.example.gradecalc.ui.theme.GradeCalcTheme

class MainActivity : ComponentActivity() {
    private var viewModel: GradeViewModel? = null

    override fun onStart() {
        super.onStart()
        viewModel?.onAppStarted()
    }

    override fun onStop() {
        viewModel?.onAppStopped(isChangingConfigurations)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: GradeViewModel = viewModel()
            viewModel = vm
            val language by vm.language.collectAsState()
            val locked by vm.locked.collectAsState()
            val pinEnabled by vm.pinEnabled.collectAsState()

            // With a PIN set, hide the app from screenshots and the recent-apps preview.
            LaunchedEffect(pinEnabled) {
                if (pinEnabled) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            WithLanguage(language) {
                GradeCalcTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        if (locked) {
                            LockScreen(
                                initialLockoutMs = vm.lockoutRemainingMs(),
                                onSubmit = vm::unlock,
                                onForgotConfirmed = vm::eraseEverything,
                            )
                        } else {
                            AppNavigation(vm)
                        }
                    }
                }
            }
        }
    }
}
