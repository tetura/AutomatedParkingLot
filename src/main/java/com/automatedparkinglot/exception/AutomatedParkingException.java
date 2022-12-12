package com.automatedparkinglot.exception;

import com.automatedparkinglot.enums.AutomatedParkingExceptionCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception to be thrown if there is a problematic case
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@Getter
public class AutomatedParkingException extends RuntimeException {

  private final AutomatedParkingExceptionCode exceptionCode;

  public AutomatedParkingException(AutomatedParkingExceptionCode exceptionCode) {
    super(exceptionCode.getExplanatoryMessage());
    this.exceptionCode = exceptionCode;
  }

}
