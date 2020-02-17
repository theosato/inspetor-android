package com.legiti

import android.content.Context
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.mockito.*
import com.legiti.helpers.InvalidCredentials
import java.util.*


@RunWith(RobolectricTestRunner::class)
class UnitTests {

    private val AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmluY2lwYWxJZCI6Imluc3BldG9yX3Rlc3QifQ.cJimBzTsFCC5LMurLelIax_-0ejXYEOZdYIL7Q3GEEQ"

    @Test(expected = InvalidCredentials::class)
    fun testSetContextWithoutConfig() {
        Inspetor.sharedInstance().setContext(
            context = Mockito.mock(Context::class.java)
        )
    }

    @Test(expected = InvalidCredentials::class)
    fun testSetupWithoutAuthToken() {
        InspetorConfig("", false)
    }

    @Test(expected = InvalidCredentials::class)
    fun testSetupWithInvalidAuthToken() {
        InspetorConfig("123", false)
    }

    @Test(expected = InvalidCredentials::class)
    fun testSetupWithAuthTokenMissingPart() {
        val invalidAuthToken = AUTH_TOKEN.split(".").subList(0, 1).joinToString(".")
        InspetorConfig(invalidAuthToken, false)
    }

    @Test(expected = InvalidCredentials::class)
    fun testSetupWithTokenMissingPrincipalId() {
        val splittedToken = AUTH_TOKEN.split(".")
        val middlePart = "{\"missing_principal_id\": \"not_principal_id\"}".toByteArray()
        val encodedMiddlePart = Base64.getEncoder().encodeToString(middlePart)
        val invalidAuthToken = arrayOf(splittedToken[0], encodedMiddlePart, splittedToken[2]).joinToString(".")

        InspetorConfig(invalidAuthToken, false)
    }

    @Test
    fun testSetupWithAuthTokenInUpperCase() {
        val splittedToken = AUTH_TOKEN.split(".")
        val middlePart = ("{\"principalId\": \"inspetor_test_SANDBOX\"}").toByteArray()
        val encodedMiddlePart = Base64.getEncoder().encodeToString(middlePart)
        val authToken = arrayOf(splittedToken[0], encodedMiddlePart, splittedToken[2]).joinToString(".")

        assertTrue(InspetorConfig.isValid(authToken))
    }

    @Test(expected = InvalidCredentials::class)
    fun testUserCreationWithoutConfig() {
        InspetorClient().trackUserCreation("123")
    }

    @Test(expected = InvalidCredentials::class)
    fun testUserUpdateWithoutConfig() {
        InspetorClient().trackUserUpdate("123")
    }

    @Test(expected = InvalidCredentials::class)
    fun testAuthLoginWithoutConfig() {
        InspetorClient().trackLogin("123", "123")
    }

    @Test(expected = InvalidCredentials::class)
    fun testAuthLogoutWithoutConfig() {
        InspetorClient().trackLogout("123", null)
    }

    @Test(expected = InvalidCredentials::class)
    fun testPassRecoveryWithoutConfig() {
        InspetorClient().trackPasswordRecovery("email@email.com")
    }

    @Test(expected = InvalidCredentials::class)
    fun testPassResetWithoutConfig() {
        InspetorClient().trackPasswordReset("123")
    }

    @Test(expected = InvalidCredentials::class)
    fun testOrderCreationWithoutConfig() {
        InspetorClient().trackOrderCreation("123")
    }
}