package com.automatedparkinglot.repositories;

import com.automatedparkinglot.entities.Floor;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A CrudRepository to handle database operations of parking lot floors
 */
@Repository
public interface FloorRepository extends CrudRepository<Floor, Long> {

  /**
   * A query to find a floor by its ordinal number from the ground
   * @param floorNumber The ordinal number of the floor from the ground to be found
   * @return Parking lot floor with this ordinal number from the ground
   */
  @Query("SELECT f FROM Floor f WHERE f.number = :floorNumber")
  Floor findFloorByFloorNumber(Integer floorNumber);

  /**
   * A query to find all parking lot floors that fit the specs of a car to be parked and contains available parking spaces
   * @param carHeight The height of the car to be parked and for which a suitable floor is being searched
   * @param carWeight The weight of the car to be parked and for which a suitable floor is being searched
   * @return All parking lot floors meeting the requirements
   */
  @Query("SELECT f FROM Floor f "
      + "WHERE f.number IN (SELECT ps.floor FROM ParkingSpace ps WHERE ps.occupyingCarId IS NULL) "
      + "AND f.ceilingHeight >= :carHeight AND :carWeight <= f.allowedWeight")
  List<Floor> findAllFittingAndAvailableFloors(BigDecimal carHeight, BigDecimal carWeight);
}
