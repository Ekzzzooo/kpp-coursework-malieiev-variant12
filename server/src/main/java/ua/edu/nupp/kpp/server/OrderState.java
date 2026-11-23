package ua.edu.nupp.kpp.server;

/** Стани життєвого циклу заявки на прокат. */
public enum OrderState {
    CREATED,    // СТВОРЕНО
    CONFIRMED,  // ПІДТВЕРДЖЕНО
    ISSUED,     // ВИДАНО
    RETURNED    // ПОВЕРНУТО
}
