package ru.yandex.practicum.dto;

public class NotAuthorizedUserException extends RuntimeException {
    public NotAuthorizedUserException() {}
    public NotAuthorizedUserException(String message) { super(message); }
}
