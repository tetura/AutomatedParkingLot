package com.automatedparkinglot.controllers;

import com.automatedparkinglot.exception.AutomatedParkingException;
import com.automatedparkinglot.exception.AutomatedParkingExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AutomatedParkingExceptionHandler {

  private final Logger logger = LoggerFactory.getLogger(AutomatedParkingExceptionHandler.class);

  /**
   * Process an exception and prepares the response to be returned once it's thrown
   *
   * @param automatedParkingException Thrown instance of the custom exception,
   *                                  AutomatedParkingException
   * @return Prepared response which is to be returned
   */
  @ExceptionHandler(AutomatedParkingException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public AutomatedParkingExceptionResponse handleRequirementException(
      AutomatedParkingException automatedParkingException) {
    logger.error("PARKING ERROR!", automatedParkingException);
    var exceptionResponse = new AutomatedParkingExceptionResponse();
    exceptionResponse.setErrorCode(automatedParkingException.getExceptionCode().name());
    exceptionResponse.setErrorMessage(
        automatedParkingException.getExceptionCode().getExplanatoryMessage());
    return exceptionResponse;
  }

}
