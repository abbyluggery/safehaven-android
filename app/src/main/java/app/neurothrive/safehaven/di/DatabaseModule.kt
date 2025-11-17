package app.neurothrive.safehaven.di

import android.content.Context
import androidx.room.Room
import app.neurothrive.safehaven.data.database.AppDatabase
import app.neurothrive.safehaven.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database Dependency Injection Module
 * Provides Room database and DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // MVP only, remove for production
            .build()
    }

    @Provides
    @Singleton
    fun provideSafeHavenProfileDao(database: AppDatabase): SafeHavenProfileDao {
        return database.safeHavenProfileDao()
    }

    @Provides
    @Singleton
    fun provideIncidentReportDao(database: AppDatabase): IncidentReportDao {
        return database.incidentReportDao()
    }

    @Provides
    @Singleton
    fun provideVerifiedDocumentDao(database: AppDatabase): VerifiedDocumentDao {
        return database.verifiedDocumentDao()
    }

    @Provides
    @Singleton
    fun provideEvidenceItemDao(database: AppDatabase): EvidenceItemDao {
        return database.evidenceItemDao()
    }

    @Provides
    @Singleton
    fun provideLegalResourceDao(database: AppDatabase): LegalResourceDao {
        return database.legalResourceDao()
    }

    @Provides
    @Singleton
    fun provideSurvivorProfileDao(database: AppDatabase): SurvivorProfileDao {
        return database.survivorProfileDao()
    }
}
