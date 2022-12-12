package com.automatedparkinglot.services;

import com.automatedparkinglot.entities.ParkingSpace;
import com.automatedparkinglot.repositories.ParkingSpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service to assign a car to a parking space or empty a parking space
 */
@Service
public class ParkingSpaceService {

  private final ParkingSpaceRepository parkingSpaceRepository;

  @Autowired
  public ParkingSpaceService(ParkingSpaceRepository parkingSpaceRepository) {
    this.parkingSpaceRepository = parkingSpaceRepository;
  }

  /**
   * Parks a car in (assigns a car to) an available parking slot on the most appropriate floor for
   * it
   *
   * @param floorNumber The ordinal number of the floor on which the car is to be parked
   * @param carId       The ID of the car to be parked
   * @return The parking space in/to which the car has been parked/assigned
   */
  public ParkingSpace parkCarInAFreeParkingSpace(Integer floorNumber, String carId) {
    var availableParkingSpacesOnFloor = parkingSpaceRepository.findAvailableParkingSpacesOnFloor(
        floorNumber);
    var parkingSpace = availableParkingSpacesOnFloor.get(
        0); // Assign car to the first available space on the floor
    parkingSpace.setOccupyingCarId(carId);
    return parkingSpaceRepository.save(parkingSpace);
  }

  /**
   * Empties a parking space from which the parking car is to be pulled out of the parking lot
   *
   * @param carId The ID of the car to be pulled out to empty the assigned parking space
   */
  public void emptyParkingSpace(String carId) {
    var occupiedParkingSpace = parkingSpaceRepository.findParkingSpaceByOccupyingCarId(carId);
    occupiedParkingSpace.setOccupyingCarId(null);
    parkingSpaceRepository.save(occupiedParkingSpace);
  }

  /**
   * Find a parking space by the occupying car's ID
   *
   * @param carId The ID of the car occupying the parking space
   * @return The parking space occupied by the car
   */
  public ParkingSpace findParkingSpaceOccupiedByCar(String carId) {
    return parkingSpaceRepository.findParkingSpaceByOccupyingCarId(carId);
  }

}
