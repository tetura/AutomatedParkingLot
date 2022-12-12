package com.automatedparkinglot.services;

import com.automatedparkinglot.dtos.ParkingRequest;
import com.automatedparkinglot.entities.ParkingRecord;
import com.automatedparkinglot.enums.AutomatedParkingExceptionCode;
import com.automatedparkinglot.enums.ParkingStatus;
import com.automatedparkinglot.exception.AutomatedParkingException;
import com.automatedparkinglot.repositories.ParkingRecordRepository;
import com.automatedparkinglot.util.PlaceholderPrinter;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service to process parking and pull out requests
 */
@Service
public class AutomatedParkingService {

  private final ParkingRecordRepository parkingRecordRepository;
  private final FloorService floorService;
  private final ParkingSpaceService parkingSpaceService;
  private final BillService billService;

  /**
   * An overloaded constructor of the class
   *
   * @param parkingRecordRepository A {@link ParkingRecordRepository} instance
   * @param floorService            A {@link FloorService} instance
   * @param parkingSpaceService     A {@link ParkingSpaceService} instance
   * @param billService             A {@link BillService} instance
   */
  @Autowired
  public AutomatedParkingService(ParkingRecordRepository parkingRecordRepository,
      FloorService floorService,
      ParkingSpaceService parkingSpaceService, BillService billService) {
    this.parkingRecordRepository = parkingRecordRepository;
    this.floorService = floorService;
    this.parkingSpaceService = parkingSpaceService;
    this.billService = billService;
  }

  /**
   * Processes parking requests to try to park a car entering the lot
   *
   * @param parkingRequest Parking request to transfer information of a car to be parked
   */
  @Transactional
  public void parkCarInAParkingSpace(ParkingRequest parkingRequest) {
    this.validateParkingRequest(parkingRequest);

    PlaceholderPrinter.printMovementInformation(
        String.format("The car %s is being transported to the parking lot.",
            parkingRequest.getCarId()));

    // First, find the best floor for the car
    var floor = floorService.findBestFloorForCar(parkingRequest.getCarHeight(),
        parkingRequest.getCarWeight());

    // Second, find a parking space on the floor and assign the car to it
    var parkingSpace = parkingSpaceService.parkCarInAFreeParkingSpace(floor.getNumber(),
        parkingRequest.getCarId());

    PlaceholderPrinter.printMovementInformation(
        String.format(
            "The automated parking lot system assigned the car %s to the parking space %s on the floor %s.",
            parkingRequest.getCarId(), parkingSpace.getId(), floor.getNumber()));

    // Create a new parking record
    var parkingRecord = new ParkingRecord();
    parkingRecord.setCarId(parkingRequest.getCarId());
    parkingRecord.setCarWeight(parkingRequest.getCarWeight());
    parkingRecord.setCarHeight(parkingRequest.getCarHeight());
    parkingRecord.setAllowedWeightOnFloorBeforeParking(floor.getAllowedWeight());
    parkingRecord.setParkingTimestamp(LocalDateTime.now());
    parkingRecord.setFloor(floor.getNumber());
    parkingRecord.setParkingSpaceId(parkingSpace.getId());
    parkingRecord.setStatus(ParkingStatus.PARKING_IN_PROGRESS.name());
    parkingRecordRepository.save(parkingRecord);

    // Update floor weight
    floorService.updateFloor(floor, parkingRequest.getCarWeight(), true);
  }

  /**
   * Pulls a parking car out of the parking lot
   *
   * @param carId The ID of the car to be pulled out of the parking lot
   */
  @Transactional
  public void pullCarOutOfLotAndGenerateBill(String carId) {
    if (Objects.isNull(parkingSpaceService.findParkingSpaceOccupiedByCar(carId))) {
      throw new AutomatedParkingException(AutomatedParkingExceptionCode.NO_PARKED_CAR_WITH_THIS_ID);
    }

    PlaceholderPrinter.printMovementInformation(
        "The car " + carId + " is being transported out of the parking lot.");

    // Find corresponding parking record
    var parkingRecord = parkingRecordRepository.findOngoingParkingRecordByCarId(carId);

    // Update floor weight
    var floor = floorService.updateFloor(parkingRecord.getFloor(), parkingRecord.getCarWeight(),
        false);

    // Update parking record
    parkingRecord.setEmptyingTimestamp(LocalDateTime.now());
    parkingRecord.setStatus(ParkingStatus.PARKING_OVER.name());
    parkingRecordRepository.save(parkingRecord);

    // Update parking space
    parkingSpaceService.emptyParkingSpace(carId);

    // Generate bill
    billService.generateBill(parkingRecord, floor);
  }

  /**
   * Pre-validates a parking request to check if the car can be parked in the lot (If not, it throws
   * a relevant custom exception.)
   *
   * @param parkingRequest Parking request being verified
   */
  private void validateParkingRequest(ParkingRequest parkingRequest) {
    if (Objects.nonNull(
        parkingSpaceService.findParkingSpaceOccupiedByCar(parkingRequest.getCarId()))) {
      throw new AutomatedParkingException(AutomatedParkingExceptionCode.CAR_ALREADY_PARKED);
    }
    if (Objects.isNull(parkingRequest.getCarWeight())) {
      throw new AutomatedParkingException(AutomatedParkingExceptionCode.CAR_WEIGHT_MISSING);
    }
    if (Objects.isNull(parkingRequest.getCarHeight())) {
      throw new AutomatedParkingException(AutomatedParkingExceptionCode.CAR_HEIGHT_MISSING);
    }
    if (Objects.isNull(parkingRequest.getCarId())) {
      throw new AutomatedParkingException(AutomatedParkingExceptionCode.CAR_ID_MISSING);
    }
  }
}
