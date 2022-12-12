package com.automatedparkinglot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Exception codes and their explanatory messages to be returned in an HTTP bad request response
 * when the custom exception is thrown if there is a problematic case
 */
@Getter
@AllArgsConstructor
public enum AutomatedParkingExceptionCode {

  NO_AVAILABLE_FLOOR(
      "There is no available floor where available parking spaces exist, whose ceiling is high enough, and which is not overweight!"),
  CAR_ALREADY_PARKED("There is already a parked car in the lot with this ID!"),
  NO_PARKED_CAR_WITH_THIS_ID(
      "A parked car to be pulled out with this ID is not available in the lot!"),
  CAR_ID_MISSING("Car ID must be provided!"),
  CAR_WEIGHT_MISSING("Car scan must pass the weight of the car to the system!"),
  CAR_HEIGHT_MISSING("Car scan must pass the height of the car to the system!");

  private final String explanatoryMessage;
}
