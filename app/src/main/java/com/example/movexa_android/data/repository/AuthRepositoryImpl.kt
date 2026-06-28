package com.example.movexa_android.data.repository

import com.example.movexa_android.data.local.UserPreferences
import com.example.movexa_android.domain.model.User
import com.example.movexa_android.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : AuthRepository {

    // Simple in-memory rate limiting for mock purposes
    private val otpRequestTimes = mutableMapOf<String, Long>()
    private val RATE_LIMIT_MS = 60000L // 1 minute

    override fun getSession(): Flow<User?> = userPreferences.userData

    override fun hasSeenOnboarding(): Flow<Boolean> = userPreferences.hasSeenOnboarding

    override suspend fun setOnboardingCompleted() {
        userPreferences.setHasSeenOnboarding(true)
    }

    override suspend fun login(email: String, password: String): Result<User> {
        delay(1000) // Simulate network
        // Mock success
        val user = User("1", email, "User $email", "mock_token")
        userPreferences.saveUser(user)
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        delay(1000)
        val user = User("2", "google.user@example.com", "Google User", "google_mock_token")
        userPreferences.saveUser(user)
        return Result.success(user)
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<User> {
        delay(1000)
        val user = User("1", email, name, "mock_token")
        userPreferences.saveUser(user)
        return Result.success(user)
    }

    override suspend fun logout() {
        userPreferences.clear()
    }

    override suspend fun sendOtp(email: String): Result<Unit> {
        val lastRequest = otpRequestTimes[email] ?: 0L
        val now = System.currentTimeMillis()
        
        if (now - lastRequest < RATE_LIMIT_MS) {
            val waitSeconds = (RATE_LIMIT_MS - (now - lastRequest)) / 1000
            return Result.failure(Exception("Please wait $waitSeconds seconds before requesting another OTP"))
        }

        delay(500)
        otpRequestTimes[email] = now
        // Send OTP via API would go here
        return Result.success(Unit)
    }

    override suspend fun verifyOtp(email: String, otp: String): Result<User> {
        delay(1000)
        if (otp == "123456") {
            val user = User("1", email, "Verified User", "mock_token")
            userPreferences.saveUser(user)
            return Result.success(user)
        }
        return Result.failure(Exception("Invalid OTP"))
    }
}