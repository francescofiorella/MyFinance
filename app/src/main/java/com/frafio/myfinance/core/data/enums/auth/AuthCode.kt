package com.frafio.myfinance.core.data.enums.auth

import com.frafio.myfinance.core.data.enums.db.Languages
import com.frafio.myfinance.core.utils.getCurrentLanguage

enum class AuthCode(val code: Int, val message: String) {
    LOGIN_SUCCESS(
        1, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Login successful"
            Languages.ITALIANO.value -> "Accesso avvenuto con successo"
            else -> "Login successful" // english
        }
    ),

    GOOGLE_LOGIN_FAILURE(
        2, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Google login failed"
            Languages.ITALIANO.value -> "Accesso con Google fallito"
            else -> "Google login failed" // english
        }
    ),

    LOGIN_FAILURE(
        3, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Login failed"
            Languages.ITALIANO.value -> "Accesso fallito"
            else -> "Login failed" // english
        }
    ),

    INVALID_EMAIL(
        4, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Invalid email address"
            Languages.ITALIANO.value -> "Email non valida"
            else -> "Invalid email address" // english
        }
    ),

    WRONG_PASSWORD(
        5, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Wrong password"
            Languages.ITALIANO.value -> "Password errata"
            else -> "Wrong password" // english
        }
    ),

    USER_NOT_FOUND(
        6, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Account not found"
            Languages.ITALIANO.value -> "Account non trovato"
            else -> "Account not found" // english
        }
    ),

    USER_DISABLED(
        7, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "This account has been disabled"
            Languages.ITALIANO.value -> "Questo account è stato disabilitato"
            else -> "This account has been disabled" // english
        }
    ),

    SIGNUP_SUCCESS(
        10, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Signup successful"
            Languages.ITALIANO.value -> "Registrazione avvenuta con successo"
            else -> "Signup successful" // english
        }
    ),

    WEAK_PASSWORD(
        11, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "The password is not secure"
            Languages.ITALIANO.value -> "La password non è sicura"
            else -> "The password is not secure" // english
        }
    ),

    EMAIL_NOT_WELL_FORMED(
        12, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter a valid email address"
            Languages.ITALIANO.value -> "Inserisci un'email valida"
            else -> "Enter a valid email address" // english
        }
    ),

    EMAIL_ALREADY_ASSOCIATED(
        13, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Email already registered"
            Languages.ITALIANO.value -> "Email già registrata"
            else -> "Email already registered" // english
        }
    ),

    SIGNUP_FAILURE(
        14, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Signup failed"
            Languages.ITALIANO.value -> "Registrazione fallita"
            else -> "Signup failed" // english
        }
    ),

    EMPTY_EMAIL(
        20, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter your email"
            Languages.ITALIANO.value -> "Inserisci la tua email"
            else -> "Enter your email" // english
        }
    ),

    EMPTY_PASSWORD(
        21, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter your password"
            Languages.ITALIANO.value -> "Inserisci la tua password"
            else -> "Enter your password" // english
        }
    ),

    SHORT_PASSWORD(
        22, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Password is too short (min. 8 characters)"
            Languages.ITALIANO.value -> "Password troppo corta (min. 8 caratteri)"
            else -> "Password is too short (min. 8 characters)" // english
        }
    ),

    EMPTY_NAME(
        23, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter your full name"
            Languages.ITALIANO.value -> "Inserisci il tuo nome e cognome"
            else -> "Enter your full name" // english
        }
    ),

    EMPTY_CONFIRM_PASSWORD(
        24, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Confirm your password"
            Languages.ITALIANO.value -> "Conferma la tua password"
            else -> "Confirm your password" // english
        }
    ),

    PASSWORD_NOT_MATCH(
        25, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "The confirmation password does not match"
            Languages.ITALIANO.value -> "Le password non coincidono"
            else -> "The confirmation password does not match" // english
        }
    ),

    EMPTY_NEW_PASSWORD(
        26, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter a new password"
            Languages.ITALIANO.value -> "Inserisci una nuova password"
            else -> "Enter a new password" // english
        }
    ),

    EMPTY_CONFIRM_NEW_PASSWORD(
        26, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Confirm the new password"
            Languages.ITALIANO.value -> "Conferma la nuova password"
            else -> "Confirm the new password" // english
        }
    ),

    EMAIL_SENT(
        30, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Email sent. Check your email box"
            Languages.ITALIANO.value -> "Email inviata. Controlla la tua casella di posta"
            else -> "Email sent. Check your email box" // english
        }
    ),

    EMAIL_NOT_SENT_TOO_MANY_REQUESTS(
        31, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Too many attempts. Try again later"
            Languages.ITALIANO.value -> "Troppi tentativi. Riprova più tardi"
            else -> "Too many attempts. Try again later" // english
        }
    ),

    EMAIL_NOT_SENT(
        32, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Error! Email not sent"
            Languages.ITALIANO.value -> "Errore! Email non inviata"
            else -> "Error! Email not sent" // english
        }
    ),

    LOGOUT_SUCCESS(
        40, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User logged out"
            Languages.ITALIANO.value -> "Utente disconnesso"
            else -> "User logged out" // english
        }
    ),

    USER_LOGGED(
        100, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User logged"
            Languages.ITALIANO.value -> "Utente connesso"
            else -> "User logged" // english
        }
    ),

    USER_NOT_LOGGED(
        101, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User not logged"
            Languages.ITALIANO.value -> "Utente non connesso"
            else -> "User not logged" // english
        }
    ),

    USER_FULL_NAME_UPDATED(
        102, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User full name updated"
            Languages.ITALIANO.value -> "Nome utente aggiornato"
            else -> "User full name updated" // english
        }
    ),

    USER_FULL_NAME_NOT_UPDATED(
        103, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User full name not updated"
            Languages.ITALIANO.value -> "Aggiornamento nome utente non riuscito"
            else -> "User full name not updated" // english
        }
    ),

    PASSWORD_UPDATED(
        104, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Password updated successfully"
            Languages.ITALIANO.value -> "Password aggiornata con successo"
            else -> "Password updated successfully" // english
        }
    ),

    PASSWORD_NOT_UPDATED(
        105, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Password not updated"
            Languages.ITALIANO.value -> "Password non aggiornata"
            else -> "Password not updated" // english
        }
    ),

    WRONG_OLD_PASSWORD(
        106, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "The password is incorrect"
            Languages.ITALIANO.value -> "La password non è corretta"
            else -> "The password is incorrect" // english
        }
    )
}