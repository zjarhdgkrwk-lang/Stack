package com.stack.feature.gate.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.stack.feature.gate.GateScreen

const val GATE_ROUTE = "gate"

fun NavController.navigateToGate() {
    navigate(GATE_ROUTE) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavGraphBuilder.gateScreen(
    onGateReady: () -> Unit
) {
    composable(route = GATE_ROUTE) {
        GateScreen(onGateReady = onGateReady)
    }
}
