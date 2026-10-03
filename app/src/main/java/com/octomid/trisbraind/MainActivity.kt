package com.octomid.trisbraind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.octomid.trisbraind.ui.BiasScreen
import com.octomid.trisbraind.ui.DashboardScreen
import com.octomid.trisbraind.ui.HistoryScreen
import com.octomid.trisbraind.ui.MainViewModel
import com.octomid.trisbraind.ui.theme.TrisBrainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrisBrainTheme {
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                if (state.showHistory) {
                    HistoryScreen(
                        history = state.history,
                        onBack = viewModel::hideHistory,
                        onClear = viewModel::clearHistory
                    )
                } else if (state.showBias) {
                    BiasScreen(
                        report = state.biasReport,
                        window = state.biasWindow,
                        loading = state.biasLoading,
                        onBack = viewModel::hideBias,
                        onWindow = viewModel::setBiasWindow
                    )
                } else {
                    DashboardScreen(viewModel)
                }
            }
        }
    }
}
