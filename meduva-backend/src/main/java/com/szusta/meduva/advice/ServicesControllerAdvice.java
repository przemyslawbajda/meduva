package com.szusta.meduva.advice;

import com.szusta.meduva.exception.AlreadyExistsException;
import com.szusta.meduva.exception.ErrorMessage;
import com.szusta.meduva.exception.ServiceAlreadyExistsException;
import com.szusta.meduva.exception.notfound.ServiceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@RestControllerAdvice
public class ServicesControllerAdvice {

    @ExceptionHandler(value = ServiceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleServiceAlreadyExistsException(ServiceAlreadyExistsException ex, WebRequest request) {

        return new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(value = ServiceNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleServiceNotFoundException(ServiceNotFoundException ex, WebRequest request) {

        return new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(value = AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleAlreadyExistsException(AlreadyExistsException ex, WebRequest request) {

        return new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );
    }
}
