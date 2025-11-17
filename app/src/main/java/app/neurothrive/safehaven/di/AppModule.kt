package app.neurothrive.safehaven.di

import android.content.Context
import app.neurothrive.safehaven.util.blockchain.DocumentVerificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * App-level Dependency Injection Module
 * Provides application-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideDocumentVerificationService(
        @ApplicationContext context: Context
    ): DocumentVerificationService {
        return DocumentVerificationService(context)
    }
}
