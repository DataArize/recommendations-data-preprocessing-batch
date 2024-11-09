package com.dataarize.recommendations.exceptions;

public class FileDoesNotExistsException extends RuntimeException{
    public FileDoesNotExistsException(String message) {
        super(message);
    }
}
