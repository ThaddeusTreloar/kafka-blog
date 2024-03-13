package com.example.streams_app.biz;

public class MissingVarException extends RuntimeException {
    public MissingVarException(String name) {
        super(
            new StringBuilder()
                .append("Failed to find var '")
                .append(name)
                .append("' in environment.'")
                .toString()
        );
    }
}
