package com.automatedparkinglot.exception;

import lombok.Data;

/**
 * The structure to wrap up an HTTP bad request response once the custom exception is thrown
 */
@Data
public class AutomatedParkingExceptionResponse {
  private String errorCode;
  private String errorMessage;
}
