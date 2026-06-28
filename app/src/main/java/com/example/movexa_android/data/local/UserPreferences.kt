package com.example.movexa_android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.movexa_android.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences @Inject constructor(
    private val context: Context
) {
    private val USER_ID = stringPreferencesKey("user_id")
    private val USER_EMAIL = stringPreferencesKey("user_email")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")

    val userData: Flow<User?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID]
        val email = preferences[USER_EMAIL]
        val name = preferences[USER_NAME]
        val token = preferences[AUTH_TOKEN]

        if (id != null && email != null && name != null) {
            User(id, email, name, token)
        } else {
            null
        }
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { it[HAS_SEEN_ONBOARDING] ?: false }

    suspend fun setHasSeenOnboarding(hasSeen: Boolean) {
        context.dataStore.edit { it[HAS_SEEN_ONBOARDING] = hasSeen }
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_EMAIL] = user.email
            preferences[USER_NAME] = user.name
            user.token?.let { preferences[AUTH_TOKEN] = it }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_NAME)
            preferences.remove(AUTH_TOKEN)
        }
    }
}