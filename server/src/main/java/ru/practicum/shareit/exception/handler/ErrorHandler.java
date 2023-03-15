package ru.practicum.shareit.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.BookingDateTimeException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.user.controller.UserController;

import java.util.Objects;

@RestControllerAdvice(assignableTypes = {ItemController.class, UserController.class, BookingController.class, RequestController.class})
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException exception) {
        log.error("Arguments not found {}", exception.getMessage());

        return new ErrorResponse("NOT FOUND", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(final BadRequestException exception) {
        log.error("Invalid arguments {}", exception.getMessage());

        return new ErrorResponse("BAD REQUEST", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnsupportedStatusException(final UnsupportedStatusException exception) {
        log.error("Incorrect state {}", exception.getMessage());

        return new ErrorResponse("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingDateTimeException(final BookingDateTimeException exception) {
        log.error("Invalid arguments {}", exception.getMessage());

        return new ErrorResponse("BAD REQUEST", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidation(final ValidationException exception) {
        log.error("Invalid arguments {}", exception.getMessage());

        return new ErrorResponse("CONFLICT REQUEST", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(final MethodArgumentNotValidException exception) {
        log.error("Invalid arguments {}", exception.getMessage());

        return new ErrorResponse("BAD REQUEST", Objects.requireNonNull(exception.getFieldError()).getDefaultMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherException(final Exception exception) {
        log.error("Internal error {}", exception.getMessage(), exception);

        return new ErrorResponse("INTERNAL SERVER ERROR", exception.getMessage());
    }
}
