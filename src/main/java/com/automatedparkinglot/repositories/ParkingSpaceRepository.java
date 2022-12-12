package com.automatedparkinglot.repositories;

import com.automatedparkinglot.entities.ParkingSpace;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingSpaceRepository extends CrudRepository<ParkingSpace, Long> {

  /**
   * A query to get the list of all available (not occupied) parking spaces on a floor
   *
   * @param floorNumber The ordinal number of the floor whose available parking spaces are to be
   *                    retrieved
   * @return The list of all available parking spaces on the given floor
   */
  @Query("SELECT ps FROM ParkingSpace ps WHERE ps.occupyingCarId IS NULL AND ps.floor = :floorNumber")
  List<ParkingSpace> findAvailableParkingSpacesOnFloor(Integer floorNumber);

  /**
   * A query to retrieve an occupied parking space by the ID of the car occupying it
   *
   * @param occupyingCarId The ID of the car occupying the target parking space
   * @return The occupied parking space
   */
  @Query("SELECT ps FROM ParkingSpace ps WHERE ps.occupyingCarId = :occupyingCarId")
  ParkingSpace findParkingSpaceByOccupyingCarId(String occupyingCarId);

}
