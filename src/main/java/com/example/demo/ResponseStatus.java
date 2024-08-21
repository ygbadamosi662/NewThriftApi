package com.example.demo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseStatus
{
    private int code;
    private String message;

    public ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
