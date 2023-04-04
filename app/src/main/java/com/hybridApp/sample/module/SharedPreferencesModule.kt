package com.hybridApp.sample.module

import android.content.Context
import com.hybridApp.sample.data.datasource.PreferenceDataSource
import com.hybridApp.sample.datasource.HybridAppMenuPreferenceDataSource
import com.hybridApp.sample.datasource.UserPreferenceDataSource
import com.hybridApp.sample.util.PrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {

    @Singleton
    @Provides
    fun providePrefManager(@ApplicationContext context: Context): PrefManager {
        return PrefManager(context)
    }

    @Singleton
    @Provides
    @Named("menu-datasource")
    fun provideBelleForetMenuPreferenceDataSource(
        prefManager: PrefManager
    ): PreferenceDataSource {
        return HybridAppMenuPreferenceDataSource(prefManager)
    }

    @Singleton
    @Provides
    @Named("user-datasource")
    fun provideUserPreferenceDataSource(
        prefManager: PrefManager
    ): PreferenceDataSource {
        return UserPreferenceDataSource(prefManager)
    }
}