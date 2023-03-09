package com.waltercasis.homevision.codetest.controller;


import com.waltercasis.homevision.codetest.exceptions.BadRequestException;
import lombok.extern.java.Log;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@Log
public class HouseControllerAdvice {

    @ExceptionHandler(value={BadRequestException.class})
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleHousesPageNotFound(BadRequestException exception){
        log.warning("Bad request: "+exception.getMessage());
        return ErrorResponse.builder(exception, BAD_REQUEST,
                "Bad request api houses {}"+exception.getMessage()).build();
    }

}
