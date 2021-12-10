package com.frafio.myfinance.data.enums.auth

enum class AuthCodeEN(val code: Int, val message: String) {
    LOGIN_SUCCESS(1, "Log in successful!"),
    GOOGLE_LOGIN_FAILURE(2, "Google log in failed!"),
    LOGIN_FAILURE(3, "Log in failed!"),
    INVALID_EMAIL(4, "Invalid email."),
    WRONG_PASSWORD(5, "Wrong password."),
    USER_NOT_FOUND(6, "The email does not correspond to any account."),
    USER_DISABLED(7, "Your account has been disabled!"),

    SIGNUP_SUCCESS(10, "Sign up successful!"),
    WEAK_PASSWORD(11, "Weak password."),
    EMAIL_NOT_WELL_FORMED(12, "Invalid email."),
    EMAIL_ALREADY_ASSOCIATED(13, "The email already has an associated account."),
    PROFILE_NOT_UPDATED(14, "Sign up failed! Contact the administrator"),
    SIGNUP_FAILURE(15, "Sign up failed!"),

    EMPTY_EMAIL(20, "Enter your email."),
    EMPTY_PASSWORD(21, "Enter the password."),
    SHORT_PASSWORD(22, "The password must be at least 8 characters long!"),
    EMPTY_NAME(23, "Enter your first and last name."),
    EMPTY_PASSWORD_CONFIRM(24, "Confirm the password."),
    PASSWORD_NOT_MATCH(25, "Passwords do not match!"),

    EMAIL_SENT(30, "Email sent. Check your mail!"),
    EMAIL_NOT_SENT_TOO_MANY_REQUESTS(31, "Email not sent! Too many requests have been made."),
    EMAIL_NOT_SENT(32, "Error! Email not sent."),

    LOGOUT_SUCCESS(40, "User logged out!"),

    USER_LOGGED(100, "User logged!"),
    USER_NOT_LOGGED(101, "User not logged"),
    USER_DATA_UPDATED(102, "User data updated!"),
    USER_DATA_NOT_UPDATED(103, "User data not updated!")
}