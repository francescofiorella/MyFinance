package com.frafio.myfinance.data.enums

enum class SIGNIN_ERROR(val value: String) {
    ERROR_INVALID_EMAIL("ERROR_INVALID_EMAIL"),
    ERROR_WRONG_PASSWORD("ERROR_WRONG_PASSWORD"),
    ERROR_USER_NOT_FOUND("ERROR_USER_NOT_FOUND"),
    ERROR_USER_DISABLED("ERROR_USER_DISABLED")
}