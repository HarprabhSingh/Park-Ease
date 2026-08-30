package com.parkeasy.accounts.web;

import java.net.URI;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.parkeasy.accounts.application.AccountEmailAlreadyRegisteredException;
import com.parkeasy.accounts.application.AccountRegistrationValidationException;
import com.parkeasy.web.RequestCorrelationFilter;

@RestControllerAdvice(assignableTypes = RegistrationController.class)
public class RegistrationProblemHandler {

    @ExceptionHandler(AccountEmailAlreadyRegisteredException.class)
    ProblemDetail handleDuplicateEmail(
            AccountEmailAlreadyRegisteredException exception,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "An account with this email already exists.");
        problem.setType(URI.create("https://parkeasy.example/problems/email-already-registered"));
        problem.setTitle("Email already registered");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "EMAIL_ALREADY_REGISTERED");
        problem.setProperty("correlationId", RequestCorrelationFilter.correlationId(request));
        problem.setProperty("fieldErrors", List.of());
        return problem;
    }

    @ExceptionHandler(AccountRegistrationValidationException.class)
    ProblemDetail handleApplicationValidation(
            AccountRegistrationValidationException exception,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "The registration details violate account policy.");
        problem.setType(URI.create("https://parkeasy.example/problems/registration-validation"));
        problem.setTitle("Registration validation failed");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "REGISTRATION_VALIDATION_FAILED");
        problem.setProperty("correlationId", RequestCorrelationFilter.correlationId(request));
        problem.setProperty("fieldErrors", List.of(
                new FieldValidationError(exception.field(), exception.getMessage())));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleRequestValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<FieldValidationError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldValidationError(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid or missing fields.");
        problem.setType(URI.create("https://parkeasy.example/problems/request-validation"));
        problem.setTitle("Request validation failed");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "REQUEST_VALIDATION_FAILED");
        problem.setProperty("correlationId", RequestCorrelationFilter.correlationId(request));
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request body is missing or contains malformed JSON.");
        problem.setType(URI.create("https://parkeasy.example/problems/malformed-request"));
        problem.setTitle("Malformed request");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "MALFORMED_REQUEST");
        problem.setProperty("correlationId", RequestCorrelationFilter.correlationId(request));
        problem.setProperty("fieldErrors", List.of());
        return problem;
    }

    private record FieldValidationError(String field, String message) {
    }
}
