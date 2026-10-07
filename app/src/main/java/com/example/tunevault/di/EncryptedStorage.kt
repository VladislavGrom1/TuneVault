package com.example.tunevault.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptedStorage {
    private const val DATA_STORE_FILE = "auth_tokens.preferences_pb"
    private const val ASSOCIATED_DATA = "auth_tokens.preferences_pb"

    @Provides
    @Singleton
    fun provideAead(@ApplicationContext context: Context) : Aead {
        AeadConfig.register()
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "auth_keyset", "auth_keyset_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://auth_master_key")
            .build()
            .keysetHandle
        return keysetHandle.getPrimitive(Aead::class.java)
    }

    @Provides
    @Singleton
    fun provideEncryptedDataStore(
        @ApplicationContext context: Context,
        aead: Aead
    ) : DataStore<Preferences> {
        val aeadSerializer = AeadSerializer(
            aead = aead,
            wrappedSerializer = PreferencesFileSerializer,
            associatedData = ASSOCIATED_DATA.encodeToByteArray()
        )

        return DataStoreFactory.create(
            serializer = aeadSerializer,
            produceFile = {context.dataStoreFile(DATA_STORE_FILE)}
        )
    }
}