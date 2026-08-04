package ru.yandex.practicum.order.exception;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.dto.ErrorResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalState(IllegalStateException ex) {
        log.error("Ошибка состояния заказа: {}", ex.getMessage());
        return new ErrorResponse("Bad Request", ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRuntime(RuntimeException ex) {
        log.error("Ошибка: {}", ex.getMessage());
        return new ErrorResponse("Bad Request", ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(FeignException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleFeign(FeignException ex) {
        log.error("Ошибка вызова внешнего сервиса: {}", ex.getMessage());
        return new ErrorResponse("Service Unavailable", "Внешний сервис недоступен", HttpStatus.BAD_GATEWAY.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll(Exception ex) {
        log.error("Необработанное исключение", ex);
        return new ErrorResponse("Internal Server Error", "Произошла ошибка", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
