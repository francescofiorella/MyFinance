package com.frafio.myfinance.core.data.enums

import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuthCodeTest {

    @Test
    fun codesTheViewModelsBranchOn_areStable() {
        assertThat(AuthCode.LOGIN_SUCCESS.code).isEqualTo(1)
        assertThat(AuthCode.SIGNUP_SUCCESS.code).isEqualTo(10)
        assertThat(AuthCode.EMPTY_EMAIL.code).isEqualTo(20)
        assertThat(AuthCode.EMPTY_PASSWORD.code).isEqualTo(21)
        assertThat(AuthCode.SHORT_PASSWORD.code).isEqualTo(22)
        assertThat(AuthCode.EMPTY_NAME.code).isEqualTo(23)
        assertThat(AuthCode.EMPTY_CONFIRM_PASSWORD.code).isEqualTo(24)
        assertThat(AuthCode.PASSWORD_NOT_MATCH.code).isEqualTo(25)
        assertThat(AuthCode.EMAIL_SENT.code).isEqualTo(30)
        assertThat(AuthCode.LOGOUT_SUCCESS.code).isEqualTo(40)
        assertThat(AuthCode.USER_LOGGED.code).isEqualTo(100)
        assertThat(AuthCode.USER_NOT_LOGGED.code).isEqualTo(101)
        assertThat(AuthCode.USER_FULL_NAME_UPDATED.code).isEqualTo(102)
        assertThat(AuthCode.PASSWORD_UPDATED.code).isEqualTo(104)
        assertThat(AuthCode.WRONG_OLD_PASSWORD.code).isEqualTo(106)
    }

    @Test
    fun everyEntry_hasAMessage() {
        AuthCode.entries.forEach { entry ->
            assertThat(entry.message).isNotEmpty()
        }
    }

    @Test
    fun codes_areUnique() {
        val duplicated = AuthCode.entries.groupBy { it.code }.filterValues { it.size > 1 }
        assertThat(duplicated).isEmpty()
    }

    @Test
    fun messages_areEnglishUnderTheTestJvmLocale() {
        assertThat(AuthCode.LOGIN_SUCCESS.message).isEqualTo("Login successful")
        assertThat(AuthCode.EMPTY_EMAIL.message).isEqualTo("Enter your email")
        assertThat(AuthCode.SHORT_PASSWORD.message).isEqualTo("Password is too short (min. 8 characters)")
    }

    @Test
    fun authResult_exposesCodeAndMessage() {
        val result = AuthResult(AuthCode.LOGIN_SUCCESS)
        assertThat(result.code).isEqualTo(AuthCode.LOGIN_SUCCESS.code)
        assertThat(result.message).isEqualTo(AuthCode.LOGIN_SUCCESS.message)
    }

    @Test
    fun authResult_customMessageOverridesTheEnumMessage() {
        val result = AuthResult(AuthCode.LOGIN_FAILURE, "Custom")
        assertThat(result.code).isEqualTo(AuthCode.LOGIN_FAILURE.code)
        assertThat(result.message).isEqualTo("Custom")
    }
}
