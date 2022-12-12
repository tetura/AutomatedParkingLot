package com.automatedparkinglot.services;

import com.automatedparkinglot.entities.Floor;
import com.automatedparkinglot.enums.AutomatedParkingExceptionCode;
import com.automatedparkinglot.exception.AutomatedParkingException;
import com.automatedparkinglot.repositories.FloorRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service to update a floor's weight and to find the best floor for a car to be parked
 */
@Service
public class FloorService {

  private final FloorRepository floorRepository;

  @Autowired
  public FloorService(FloorRepository floorRepository) {
    this.floorRepository = floorRepository;
  }

  /**
   * Updates the weight of a floor after a car left or parked
   *
   * @param floorToBeUpdated The floor to be updated
   * @param carWeight        The weight of the car that is being parked or has left the lot
   * @param isCarToBeParked  Whether a car is being parked or has left the lot
   * @return The updated floor
   */
  public Floor updateFloor(Floor floorToBeUpdated, BigDecimal carWeight, boolean isCarToBeParked) {
    floorToBeUpdated.setAllowedWeight(
        !isCarToBeParked ? floorToBeUpdated.getAllowedWeight().add(carWeight)
            : floorToBeUpdated.getAllowedWeight().subtract(carWeight));
    return floorRepository.save(floorToBeUpdated);
  }

  public Floor updateFloor(Integer numberOfFloorToBeUpdated, BigDecimal carWeight,
      boolean isCarToBeParked) {
    var floorToBeUpdated = floorRepository.findFloorByFloorNumber(numberOfFloorToBeUpdated);
    return this.updateFloor(floorToBeUpdated, carWeight, isCarToBeParked);
  }

  /**
   * Finds the best floor for a car to be parked based on the car's weight and height
   *
   * @param carHeight The height of the car to be parked
   * @param carWeight The weight of the car to be parked
   * @return The found best floor
   */
  public Floor findBestFloorForCar(BigDecimal carHeight, BigDecimal carWeight) {
    var allFittingAndAvailableFloors = floorRepository.findAllFittingAndAvailableFloors(carHeight,
        carWeight);
    if (Objects.nonNull(allFittingAndAvailableFloors) && !allFittingAndAvailableFloors.isEmpty()) {
      // Space must be saved principally.
      // Reference: https://en.wikipedia.org/wiki/Automated_parking_system#Space_saving
      // Find the floor whose ceiling height is closest to the car's height
      var floorClosestToCarHeight = allFittingAndAvailableFloors.stream()
          .min(Comparator.comparing(floor -> carHeight.subtract(floor.getCeilingHeight()).abs()));
      return floorClosestToCarHeight.orElse(null);
    } else {
      throw new AutomatedParkingException(AutomatedParkingExceptionCode.NO_AVAILABLE_FLOOR);
    }
  }
}
