package ua.edu.nupp.kpp.client;

/** Стани життєвого циклу заявки (збігаються з серверними). */
public enum OrderState {
    CREATED, CONFIRMED, ISSUED, RETURNED
}
