package com.automatedparkinglot.repositories;

import com.automatedparkinglot.entities.ParkingRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A CrudRepository to handle database operations of parking bills
 */
@Repository
public interface ParkingRecordRepository extends CrudRepository<ParkingRecord, Long> {

  /**
   * A query to find the in-progress parking record of a car
   *
   * @param carId The ID of the car which is still parking
   * @return The expected parking record
   */
  @Query("SELECT pr FROM ParkingRecord pr WHERE pr.carId = :carId AND pr.status = 'PARKING_IN_PROGRESS'")
  ParkingRecord findOngoingParkingRecordByCarId(String carId);

}
