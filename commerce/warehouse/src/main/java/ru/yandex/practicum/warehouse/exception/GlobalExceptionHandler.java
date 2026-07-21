package ru.yandex.practicum.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.dto.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleProductAlreadyInWarehouse() {}

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleLowQuantity() {}

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleNoProductInWarehouse() {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}