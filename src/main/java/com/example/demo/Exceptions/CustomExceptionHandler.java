package com.example.demo.Exceptions;

import jakarta.validation.UnexpectedTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler
{
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> MethodArgumentNotValidExceptionFunction(MethodArgumentNotValidException e)
    {
        Map<String,Object> res = new HashMap<>();
        res.put("msg","Validation Error");
        res.put("ResponseStatus","400");
        List<Object> list = new ArrayList<>();
        e.getFieldErrors().forEach((error)-> {
            Map map = new HashMap<>();
            map.put("field",error.getField());
            map.put("message",error.getDefaultMessage());
            map.put("Object",error.getObjectName());
            list.add(map);
        });
        res.put("data",list);

        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<?> HttpMessageNotReadableExceptionFunction(HttpMessageNotReadableException e)
    {
        Map<String,Object> res = new HashMap<>();
        res.put("error", "JSON parse error");
        res.put("msg", e.getMessage());

        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

}
