package com.frafio.myfinance.data.enums.auth
import com.frafio.myfinance.data.enums.db.Languages
import com.frafio.myfinance.utils.getCurrentLanguage

object AuthCode {
    fun getMessageOf(code: Int): String {
        return when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> AuthCodeEN.getMessageOf(code)
            Languages.ITALIANO.value -> AuthCodeIT.getMessageOf(code)
            else -> AuthCodeEN.getMessageOf(code)
        }
    }

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
        USER_DATA_NOT_UPDATED(103, "User data not updated!");

        fun getMessageOf(authCode: Int) : String {
            return when (authCode) {
                LOGIN_SUCCESS.code -> LOGIN_SUCCESS.message
                GOOGLE_LOGIN_FAILURE.code -> GOOGLE_LOGIN_FAILURE.message
                LOGIN_FAILURE.code -> LOGIN_FAILURE.message
                INVALID_EMAIL.code -> INVALID_EMAIL.message
                WRONG_PASSWORD.code -> WRONG_PASSWORD.message
                USER_NOT_FOUND.code -> USER_NOT_FOUND.message
                USER_DISABLED.code -> USER_DISABLED.message
                SIGNUP_SUCCESS.code -> SIGNUP_SUCCESS.message
                WEAK_PASSWORD.code -> WEAK_PASSWORD.message
                EMAIL_NOT_WELL_FORMED.code -> EMAIL_NOT_WELL_FORMED.message
                EMAIL_ALREADY_ASSOCIATED.code -> EMAIL_ALREADY_ASSOCIATED.message
                PROFILE_NOT_UPDATED.code -> PROFILE_NOT_UPDATED.message
                SIGNUP_FAILURE.code -> SIGNUP_FAILURE.message
                EMPTY_EMAIL.code -> EMPTY_EMAIL.message
                EMPTY_PASSWORD.code -> EMPTY_PASSWORD.message
                SHORT_PASSWORD.code -> SHORT_PASSWORD.message
                EMPTY_NAME.code -> EMPTY_NAME.message
                EMPTY_PASSWORD_CONFIRM.code -> EMPTY_PASSWORD_CONFIRM.message
                PASSWORD_NOT_MATCH.code -> PASSWORD_NOT_MATCH.message
                EMAIL_SENT.code -> EMAIL_SENT.message
                EMAIL_NOT_SENT_TOO_MANY_REQUESTS.code -> EMAIL_NOT_SENT_TOO_MANY_REQUESTS.message
                EMAIL_NOT_SENT.code -> EMAIL_NOT_SENT.message
                LOGOUT_SUCCESS.code -> LOGOUT_SUCCESS.message
                USER_LOGGED.code -> USER_LOGGED.message
                USER_NOT_LOGGED.code -> USER_NOT_LOGGED.message
                USER_DATA_UPDATED.code -> USER_DATA_UPDATED.message
                USER_DATA_NOT_UPDATED.code -> USER_DATA_NOT_UPDATED.message
                else -> "Error" // should not be possible
            }
        }
    }

    enum class AuthCodeIT(val code: Int, val message: String) {
        LOGIN_SUCCESS(1, "Accesso avvenuto con successo!"),
        GOOGLE_LOGIN_FAILURE(2, "Accesso con Google fallito!"),
        LOGIN_FAILURE(3, "Accesso fallito!"),
        INVALID_EMAIL(4, "L'email inserita non è ben formata."),
        WRONG_PASSWORD(5, "La password inserita non è corretta."),
        USER_NOT_FOUND(6, "L'email inserita non ha un account associato."),
        USER_DISABLED(7, "Il tuo account è stato disabilitato!"),

        SIGNUP_SUCCESS(10, "Registrazione avvenuta con successo!"),
        WEAK_PASSWORD(11, "La password inserita non è sicura."),
        EMAIL_NOT_WELL_FORMED(12, "L'email inserita non è ben formata."),
        EMAIL_ALREADY_ASSOCIATED(13, "L'email inserita ha già un account associato."),
        PROFILE_NOT_UPDATED(14, "Registrazione non avvenuta correttamente! Contatta l'amministratore"),
        SIGNUP_FAILURE(15, "Registrazione fallita!"),

        EMPTY_EMAIL(20, "Inserisci la tua email."),
        EMPTY_PASSWORD(21, "Inserisci la password."),
        SHORT_PASSWORD(22, "La password deve essere lunga almeno 8 caratteri!"),
        EMPTY_NAME(23, "Inserisci nome e cognome."),
        EMPTY_PASSWORD_CONFIRM(24, "Inserisci nuovamente la password."),
        PASSWORD_NOT_MATCH(25, "Le password inserite non corrispondono!"),

        EMAIL_SENT(30, "Email inviata. Controlla la tua posta!"),
        EMAIL_NOT_SENT_TOO_MANY_REQUESTS(31, "Email non inviata! Sono state effettuate troppe richieste."),
        EMAIL_NOT_SENT(32, "Errore! Email non inviata."),

        LOGOUT_SUCCESS(40, "Utente disconnesso!"),

        USER_LOGGED(100, "Utente connesso!"),
        USER_NOT_LOGGED(101, "Utente non connesso"),
        USER_DATA_UPDATED(102, "Dati utente aggiornati!"),
        USER_DATA_NOT_UPDATED(103, "Aggiornamento dati utente non riuscito!");

        fun getMessageOf(authCode: Int) : String {
            return when (authCode) {
                LOGIN_SUCCESS.code -> LOGIN_SUCCESS.message
                GOOGLE_LOGIN_FAILURE.code -> GOOGLE_LOGIN_FAILURE.message
                LOGIN_FAILURE.code -> LOGIN_FAILURE.message
                INVALID_EMAIL.code -> INVALID_EMAIL.message
                WRONG_PASSWORD.code -> WRONG_PASSWORD.message
                USER_NOT_FOUND.code -> USER_NOT_FOUND.message
                USER_DISABLED.code -> USER_DISABLED.message
                SIGNUP_SUCCESS.code -> SIGNUP_SUCCESS.message
                WEAK_PASSWORD.code -> WEAK_PASSWORD.message
                EMAIL_NOT_WELL_FORMED.code -> EMAIL_NOT_WELL_FORMED.message
                EMAIL_ALREADY_ASSOCIATED.code -> EMAIL_ALREADY_ASSOCIATED.message
                PROFILE_NOT_UPDATED.code -> PROFILE_NOT_UPDATED.message
                SIGNUP_FAILURE.code -> SIGNUP_FAILURE.message
                EMPTY_EMAIL.code -> EMPTY_EMAIL.message
                EMPTY_PASSWORD.code -> EMPTY_PASSWORD.message
                SHORT_PASSWORD.code -> SHORT_PASSWORD.message
                EMPTY_NAME.code -> EMPTY_NAME.message
                EMPTY_PASSWORD_CONFIRM.code -> EMPTY_PASSWORD_CONFIRM.message
                PASSWORD_NOT_MATCH.code -> PASSWORD_NOT_MATCH.message
                EMAIL_SENT.code -> EMAIL_SENT.message
                EMAIL_NOT_SENT_TOO_MANY_REQUESTS.code -> EMAIL_NOT_SENT_TOO_MANY_REQUESTS.message
                EMAIL_NOT_SENT.code -> EMAIL_NOT_SENT.message
                LOGOUT_SUCCESS.code -> LOGOUT_SUCCESS.message
                USER_LOGGED.code -> USER_LOGGED.message
                USER_NOT_LOGGED.code -> USER_NOT_LOGGED.message
                USER_DATA_UPDATED.code -> USER_DATA_UPDATED.message
                USER_DATA_NOT_UPDATED.code -> USER_DATA_NOT_UPDATED.message
                else -> "Errore" // should not be possible
            }
        }
    }
}