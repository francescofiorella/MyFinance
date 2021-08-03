package com.frafio.myfinance.data.enums

enum class AUTH_RESULT(val code: Int, val message: String) {
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

    EMPTY_EMAIL(20, "Inserisci l'email."),
    EMPTY_PASSWORD(21, "Inserisci la password."),
    SHORT_PASSWORD(22, "La password deve essere lunga almeno 8 caratteri!"),
    EMPTY_NAME(23, "Inserisci nome e cognome."),
    EMPTY_PASSWORD_CONFIRM(24, "Inserisci nuovamente la password."),
    PASSWORD_NOT_MATCH(25, "Le password inserite non corrispondono!"),

    EMAIL_SENT(30, "Email inviata. Controlla la tua posta!"),
    EMAIL_NOT_SENT_TOO_MANY_REQUESTS(31, "Email non inviata! Sono state effettuate troppe richieste."),
    EMAIL_NOT_SENT(32, "Errore! Email non inviata."),

    LOGOUT_SUCCESS(40, "User logged out!"),

    USER_LOGGED(100, "User logged!"),
    USER_NOT_LOGGED(101, "User not logged"),
    USER_DATA_UPDATED(102, "User data updated!"),
    USER_DATA_NOT_UPDATED(103, "Aggiornamento dati utente non riuscito!")
}