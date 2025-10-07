package com.ke.biliblli.common

import android.content.Context
import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.ke.biliblli.api.BilibiliApi
import com.ke.biliblli.api.BilibiliHtmlApi
import com.ke.biliblli.common.http.BilibiliHttpInterceptor
import com.ke.biliblli.common.http.BilibiliProtoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


class KePersistentCookieJar(
    cache: CookieCache,
    persistor: CookiePersistor,
    private val bilibiliStorage: BilibiliStorage
) : PersistentCookieJar(cache, persistor) {


    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        if (bilibiliStorage.tryLook && url.encodedPath == "/x/player/wbi/playurl") {
            return emptyList()
        }
        return super.loadForRequest(url)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        super.saveFromResponse(url, cookies)
    }

    fun deleteAllCookie() {
        clear()
    }


    fun getCsrf(): String {
        return loadForRequest(BilibiliApi.baseUrl.toHttpUrl())
            .firstOrNull {
                it.name == "bili_jct"
            }?.value ?: ""
    }

}

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Provides
    @Singleton
    fun provideCookieJar(
        @ApplicationContext context: Context,
        bilibiliStorage: BilibiliStorage
    ): KePersistentCookieJar {


        return KePersistentCookieJar(
            SetCookieCache(),
            SharedPrefsCookiePersistor(context),
            bilibiliStorage
        )
    }


    @Provides
    @Singleton
    fun provideHttpClient(
        cookieJar: KePersistentCookieJar,
        bilibiliHttpInterceptor: BilibiliHttpInterceptor
    ): OkHttpClient {
        val logger = HttpLoggingInterceptor {
            Log.d("Http", it)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient
            .Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .addInterceptor(bilibiliHttpInterceptor)
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideProtoApi(
        client: OkHttpClient
    ): BilibiliProtoApi {


        return Retrofit.Builder()
            .client(client)
            .baseUrl(BilibiliApi.baseUrl)
            .addConverterFactory(
                ProtoConverterFactory.create()
            ).build()
            .create(BilibiliProtoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHtmlApi(client: OkHttpClient): BilibiliHtmlApi {
        return Retrofit.Builder()
            .baseUrl(BilibiliApi.baseUrl)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(BilibiliHtmlApi::class.java)
    }

    @Provides
    @Singleton
    fun provideApi(
        client: OkHttpClient
    ): BilibiliApi {


        val json = Json {
            ignoreUnknownKeys = true
        }
        return Retrofit.Builder()
            .client(client)
            .baseUrl(BilibiliApi.baseUrl)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json; charset=UTF8".toMediaType()
                )
            ).build()
            .create(BilibiliApi::class.java)
    }

}

