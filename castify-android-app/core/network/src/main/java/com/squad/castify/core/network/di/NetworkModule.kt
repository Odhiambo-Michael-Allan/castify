package com.squad.castify.core.network.di

import android.content.Context
import androidx.tracing.trace
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.util.DebugLogger
import com.squad.castify.core.network.BuildConfig
import com.squad.castify.core.network.demo.DemoAssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn( SingletonComponent::class )
object NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun providesDemoAssetManager(
        @ApplicationContext context: Context
    ): DemoAssetManager = DemoAssetManager( context.assets::open )

    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory = trace( "CastifyOkHttpClient" ) {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        if ( BuildConfig.DEBUG ) {
                            setLevel( HttpLoggingInterceptor.Level.BODY )
                        }
                    }
            ).build()
    }

    /**
     * Since we're displaying SVGs in the app, Coil needs an ImageLoader which supports this
     * format. During Coil's initialization it will call 'applicationContext.newImageLoader()' to
     * obtain an ImageLoader.
     *
     * @see <a href="https://github.com/coil-kt/coil/blob/main/coil-singleton/src/main/java/coil/Coil.kt">Coil</a>
     */
    @Provides
    @Singleton
    fun imageLoader(
        // We specifically request dagger.Lazy here, so that it's not instantiated from Dagger.
        okHttpCallFactory: dagger.Lazy<Call.Factory>,
        @ApplicationContext application: Context
    ): ImageLoader = trace( "NiaImageLoader" ) {
        ImageLoader.Builder( application )
            .callFactory { okHttpCallFactory.get() }
            .components { add( SvgDecoder.Factory() ) }
            // Assume most content images are versioned urls but some problematic
            // images are fetching each time.
            .respectCacheHeaders( false )
            .apply {
                if ( BuildConfig.DEBUG ) {
                    logger( DebugLogger() )
                }
            }.build()
    }

}