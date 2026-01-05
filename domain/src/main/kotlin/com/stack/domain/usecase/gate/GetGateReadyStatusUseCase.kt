package com.stack.domain.usecase.gate

import com.stack.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to observe the GateReady status.
 * Returns true if user has completed the onboarding flow.
 */
class GetGateReadyStatusUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return settingsRepository.observeGateCompleted()
    }
}
