package com.example.csemaster.entity;


import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ExceptionEnum {
    // System Exception (일반적인 예외)
    RUNTIME_EXCEPTION(HttpStatus.BAD_REQUEST, "runtime error"),
    ACCESS_DENIED_EXCEPTION(HttpStatus.UNAUTHORIZED, "access denied"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "server error"),

    // JWT Exception
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "Invalid token"),
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "Expired token"),
    UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "Unsupported token"),
    ILLEGAL_ARGUMENT(HttpStatus.UNAUTHORIZED, "Illegal argument", "Invalid type of argument requested"),
    SIGNATURE_EXCEPTION(HttpStatus.UNAUTHORIZED, "Invalid signature", "The token's signature is not valid"),

    // Custom Exception
    NOT_FOUND_SUBJECT(HttpStatus.NOT_FOUND, "not found subject","It's a subject that's not on the list"),
    NOT_FOUND_DETAIL_SUBJECT(HttpStatus.NOT_FOUND, "not found detail subject", "It's a detail subject that's not on the list"),
    DUPLICATE_SUBJECT(HttpStatus.BAD_REQUEST, "existing subject", "It's a subject that already exists"),
    DUPLICATE_DETAIL_SUBJECT(HttpStatus.BAD_REQUEST, "existing detail subject", "It's a detail subject that already exists"),

    NOT_FOUND_ID(HttpStatus.NOT_FOUND, "not found ID", "It's a ID that does not exist in the database"),
    INCORRECT_QUIZ_CONTENT(HttpStatus.BAD_REQUEST, "incorrect quiz content", "Type condition of json content in quiz not met"),
    INVALID_IDENTIFIER(HttpStatus.UNAUTHORIZED, "invalid identifier", "Requestor's identifier is invalid"),
    NO_CHANGE(HttpStatus.BAD_REQUEST, "no changes", "There is no change"),;


    private final HttpStatus status;
    private final String error;
    private String description;

    ExceptionEnum(HttpStatus status, String error) {
        this.status = status;
        this.error = error;
    }
    ExceptionEnum(HttpStatus status, String error, String description) {
        this.status = status;
        this.error = error;
        this.description = description;
    }
}
