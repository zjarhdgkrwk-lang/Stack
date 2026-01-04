package com.stack.domain.usecase.gate

import com.stack.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * UseCase to set the GateReady status.
 * Call this when user completes the onboarding flow.
 */
class SetGateReadyStatusUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(completed: Boolean) {
        settingsRepository.setGateCompleted(completed)
    }
}
