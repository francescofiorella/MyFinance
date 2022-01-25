package com.frafio.myfinance.data.enums.auth

import com.frafio.myfinance.data.enums.db.Languages
import com.frafio.myfinance.utils.getCurrentLanguage

enum class AuthCode(val code: Int, val message: String) {
    LOGIN_SUCCESS(
        1, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Log in successful!"
            Languages.ITALIANO.value -> "Accesso avvenuto con successo!"
            else -> "Log in successful!" // english
        }
    ),

    GOOGLE_LOGIN_FAILURE(
        2, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Google log in failed!"
            Languages.ITALIANO.value -> "Accesso con Google fallito!"
            else -> "Google log in failed!" // english
        }
    ),

    LOGIN_FAILURE(
        3, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Log in failed!"
            Languages.ITALIANO.value -> "Accesso fallito!"
            else -> "Log in failed!" // english
        }
    ),

    INVALID_EMAIL(
        4, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Invalid email."
            Languages.ITALIANO.value -> "L'email inserita non è valida."
            else -> "Invalid email." // english
        }
    ),

    WRONG_PASSWORD(
        5, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Wrong password."
            Languages.ITALIANO.value -> "La password inserita non è corretta."
            else -> "Wrong password." // english
        }
    ),

    USER_NOT_FOUND(
        6, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "The email does not correspond to any account."
            Languages.ITALIANO.value -> "L'email inserita non ha un account associato."
            else -> "The email does not correspond to any account." // english
        }
    ),

    USER_DISABLED(
        7, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Your account has been disabled!"
            Languages.ITALIANO.value -> "Il tuo account è stato disabilitato!"
            else -> "Your account has been disabled!" // english
        }
    ),

    SIGNUP_SUCCESS(
        10, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Sign up successful!"
            Languages.ITALIANO.value -> "Registrazione avvenuta con successo!"
            else -> "Sign up successful!" // english
        }
    ),

    WEAK_PASSWORD(
        11, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Weak password."
            Languages.ITALIANO.value -> "La password inserita non è sicura."
            else -> "Weak password." // english
        }
    ),

    EMAIL_NOT_WELL_FORMED(
        12, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Email not well formed."
            Languages.ITALIANO.value -> "L'email inserita non è ben formata."
            else -> "Email not well formed." // english
        }
    ),

    EMAIL_ALREADY_ASSOCIATED(
        13, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "The email already has an associated account."
            Languages.ITALIANO.value -> "L'email inserita ha già un account associato."
            else -> "The email already has an associated account." // english
        }
    ),

    PROFILE_NOT_UPDATED(
        14, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Sign up failed! Contact the administrator."
            Languages.ITALIANO.value -> "Registrazione non avvenuta correttamente! Contatta l'amministratore."
            else -> "Sign up failed! Contact the administrator." // english
        }
    ),

    SIGNUP_FAILURE(
        15, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Sign up failed!"
            Languages.ITALIANO.value -> "Registrazione fallita!"
            else -> "Sign up failed!" // english
        }
    ),

    EMPTY_EMAIL(
        20, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter your email."
            Languages.ITALIANO.value -> "Inserisci la tua email."
            else -> "Enter your email." // english
        }
    ),

    EMPTY_PASSWORD(
        21, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter the password."
            Languages.ITALIANO.value -> "Inserisci la password."
            else -> "Enter the password." // english
        }
    ),

    SHORT_PASSWORD(
        22, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "The password must be at least 8 characters long!"
            Languages.ITALIANO.value -> "La password deve essere lunga almeno 8 caratteri!"
            else -> "The password must be at least 8 characters long!" // english
        }
    ),

    EMPTY_NAME(
        23, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter your first and last name."
            Languages.ITALIANO.value -> "Inserisci nome e cognome."
            else -> "Enter your first and last name." // english
        }
    ),

    EMPTY_PASSWORD_CONFIRM(
        24, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Confirm the password."
            Languages.ITALIANO.value -> "Inserisci nuovamente la password."
            else -> "Confirm the password." // english
        }
    ),

    PASSWORD_NOT_MATCH(
        25, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Passwords do not match!"
            Languages.ITALIANO.value -> "Le password inserite non corrispondono!"
            else -> "Passwords do not match!" // english
        }
    ),

    EMAIL_SENT(
        30, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Email sent. Check your mail!"
            Languages.ITALIANO.value -> "Email inviata. Controlla la tua posta!"
            else -> "Email sent. Check your mail!" // english
        }
    ),

    EMAIL_NOT_SENT_TOO_MANY_REQUESTS(
        31, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Email not sent! Too many requests have been made."
            Languages.ITALIANO.value -> "Email non inviata! Sono state effettuate troppe richieste."
            else -> "Email not sent! Too many requests have been made." // english
        }
    ),

    EMAIL_NOT_SENT(
        32, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Error! Email not sent."
            Languages.ITALIANO.value -> "Errore! Email non inviata."
            else -> "Error! Email not sent." // english
        }
    ),

    LOGOUT_SUCCESS(
        40, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User logged out!"
            Languages.ITALIANO.value -> "Utente disconnesso!"
            else -> "User logged out!" // english
        }
    ),

    USER_LOGGED(
        100, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User logged!"
            Languages.ITALIANO.value -> "Utente connesso!"
            else -> "User logged!" // english
        }
    ),

    USER_NOT_LOGGED(
        101, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User not logged!"
            Languages.ITALIANO.value -> "Utente non connesso!"
            else -> "User not logged!" // english
        }
    ),

    USER_DATA_UPDATED(
        102, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User data updated!"
            Languages.ITALIANO.value -> "Dati utente aggiornati!"
            else -> "User data updated!" // english
        }
    ),

    USER_DATA_NOT_UPDATED(
        103, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "User data not updated!"
            Languages.ITALIANO.value -> "Aggiornamento dati utente non riuscito!"
            else -> "User data not updated!" // english
        }
    );
}