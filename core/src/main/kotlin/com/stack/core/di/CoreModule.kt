package com.stack.core.di

import com.stack.core.crash.CrashCapture
import com.stack.core.crash.DefaultCrashCapture
import com.stack.core.logging.Logger
import com.stack.core.logging.TimberLogger
import com.stack.core.util.DefaultDispatcherProvider
import com.stack.core.util.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        impl: DefaultDispatcherProvider
    ): DispatcherProvider

    @Binds
    @Singleton
    abstract fun bindLogger(
        impl: TimberLogger
    ): Logger

    @Binds
    @Singleton
    abstract fun bindCrashCapture(
        impl: DefaultCrashCapture
    ): CrashCapture
}
