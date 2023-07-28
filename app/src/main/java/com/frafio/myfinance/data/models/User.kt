package com.frafio.myfinance.data.models

data class User(
    val fullName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val provider: Int? = null,
    val creationYear: Int? = null,
    val creationMonth: Int? = null,
    val creationDay: Int? = null
) {
    companion object {
        const val EMAIL_PROVIDER = 0
        const val GOOGLE_PROVIDER = 1
    }
}