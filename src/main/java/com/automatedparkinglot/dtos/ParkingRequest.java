package com.automatedparkinglot.dtos;

import java.math.BigDecimal;
import lombok.Data;

/**
 * A DTO to transfer information of a car to be parked: car's ID, car's weight, and car's height
 * Here, the provider car ID can be any unique ID, e.g. the car's licence plate code
 */
@Data
public class ParkingRequest {

  private String carId; // e.g. licence plate code
  private BigDecimal carWeight;
  private BigDecimal carHeight;

}
