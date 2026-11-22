package com.frafio.myfinance.core.data.enums.auth

import androidx.annotation.StringRes
import com.frafio.myfinance.R

enum class AuthCode(val code: Int, @StringRes val messageRes: Int) {
    LOGIN_SUCCESS(1, R.string.login_success),
    GOOGLE_LOGIN_FAILURE(2, R.string.google_login_failed),
    LOGIN_FAILURE(3, R.string.login_failed),
    INVALID_EMAIL(4, R.string.error_invalid_email),
    WRONG_PASSWORD(5, R.string.error_wrong_password),
    USER_NOT_FOUND(6, R.string.error_account_not_found),
    USER_DISABLED(7, R.string.error_account_disabled),
    SIGNUP_SUCCESS(10, R.string.signup_success),
    WEAK_PASSWORD(11, R.string.error_weak_password),
    EMAIL_NOT_WELL_FORMED(12, R.string.error_email_not_well_formed),
    EMAIL_ALREADY_ASSOCIATED(13, R.string.error_email_already_registered),
    SIGNUP_FAILURE(14, R.string.signup_failed),
    EMPTY_EMAIL(20, R.string.error_empty_email),
    EMPTY_PASSWORD(21, R.string.error_empty_password),
    SHORT_PASSWORD(22, R.string.error_short_password),
    EMPTY_NAME(23, R.string.error_empty_full_name),
    EMPTY_CONFIRM_PASSWORD(24, R.string.error_empty_confirm_password),
    PASSWORD_NOT_MATCH(25, R.string.error_password_not_match),
    EMPTY_NEW_PASSWORD(26, R.string.error_empty_new_password),
    EMPTY_CONFIRM_NEW_PASSWORD(27, R.string.error_empty_confirm_new_password),
    EMAIL_SENT(30, R.string.email_sent),
    EMAIL_NOT_SENT_TOO_MANY_REQUESTS(31, R.string.email_not_sent_too_many_requests),
    EMAIL_NOT_SENT(32, R.string.email_not_sent),
    LOGOUT_SUCCESS(40, R.string.logout_success),
    USER_LOGGED(100, R.string.user_logged),
    USER_NOT_LOGGED(101, R.string.user_not_logged),
    USER_FULL_NAME_UPDATED(102, R.string.full_name_updated),
    USER_FULL_NAME_NOT_UPDATED(103, R.string.user_full_name_not_updated),
    PASSWORD_UPDATED(104, R.string.password_updated),
    PASSWORD_NOT_UPDATED(105, R.string.password_not_updated),
    WRONG_OLD_PASSWORD(106, R.string.error_wrong_current_password)
}
