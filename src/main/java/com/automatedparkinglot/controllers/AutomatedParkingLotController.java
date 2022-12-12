package com.automatedparkinglot.controllers;

import static org.springframework.http.HttpStatus.OK;

import com.automatedparkinglot.dtos.ParkingRequest;
import com.automatedparkinglot.services.AutomatedParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/automated-parking-lot", consumes = "application/json", produces = "application/json")
public class AutomatedParkingLotController {

  private final AutomatedParkingService automatedParkingService;

  @Autowired
  private AutomatedParkingLotController(AutomatedParkingService automatedParkingService) {
    this.automatedParkingService = automatedParkingService;
  }

  /**
   * An endpoint to park a car in an available and the most suitable parking space
   *
   * @param parkingRequest A DTO to transfer information of a car to be parked
   * @return A {@link ResponseEntity} instance
   */
  @PostMapping(value = "/park")
  public ResponseEntity<Void> parkCarInAParkingSpace(@RequestBody ParkingRequest parkingRequest) {
    automatedParkingService.parkCarInAParkingSpace(parkingRequest);
    return new ResponseEntity<>(OK);
  }

  /**
   * An endpoint to pull a parked car out of the parking lot
   *
   * @param carId The ID of the car to be pulled out of the parking lot
   * @return A {@link ResponseEntity} instance
   */
  @PostMapping(value = "/pull-out-and-bill/{carId}")
  public ResponseEntity<Void> pullCarOutOfLotAndGenerateBill(@PathVariable String carId) {
    automatedParkingService.pullCarOutOfLotAndGenerateBill(carId);
    return new ResponseEntity<>(OK);
  }

}
