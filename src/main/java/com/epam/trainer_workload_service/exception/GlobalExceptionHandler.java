package com.epam.trainer_workload_service.exception;

import com.epam.trainer_workload_service.model.ErrorResponse;
import com.epam.trainer_workload_service.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BAD_REQUEST = "Bad request. transactionId={}, message={}";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE = "message";
    private static final String INTERNAL_ERROR = "Internal error. transactionId={}, message={}";
    private static final String INTERNAL_SERVER_ERROR = "Internal server error";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        String tx = MDC.get(TRANSACTION_ID);
        log.warn(BAD_REQUEST, tx, ex.getMessage());

        ErrorResponse error = new ErrorResponse();
        error.setError(ex.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex) {
        String tx = MDC.get(TRANSACTION_ID);

        log.warn("Service exception. transactionId={}, message={}", tx, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        TIMESTAMP, ZonedDateTime.now(),
                        TRANSACTION_ID, tx,
                        MESSAGE, ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        String tx = MDC.get(TRANSACTION_ID);

        log.error(INTERNAL_ERROR, tx, ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        TIMESTAMP, ZonedDateTime.now(),
                        TRANSACTION_ID, tx,
                        MESSAGE, INTERNAL_SERVER_ERROR
                ));
    }
}
