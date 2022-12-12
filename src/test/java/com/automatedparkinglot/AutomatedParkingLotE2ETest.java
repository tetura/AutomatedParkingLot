package com.automatedparkinglot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.automatedparkinglot.dtos.ParkingRequest;
import com.automatedparkinglot.enums.AutomatedParkingExceptionCode;
import com.automatedparkinglot.enums.ParkingStatus;
import com.automatedparkinglot.repositories.BillRepository;
import com.automatedparkinglot.repositories.FloorRepository;
import com.automatedparkinglot.repositories.ParkingRecordRepository;
import com.automatedparkinglot.repositories.ParkingSpaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AutomatedParkingLotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql({
    "/data.sql"}) // The same initial data set covering some floors and parking spaces can be used for these tests as well.
class AutomatedParkingLotE2ETest {

  public final CountDownLatch latch = new CountDownLatch(1);
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ParkingRecordRepository parkingRecordRepository;
  @Autowired
  private FloorRepository floorRepository;
  @Autowired
  private ParkingSpaceRepository parkingSpaceRepository;
  @Autowired
  private BillRepository billRepository;

  @Test
  void test1_parkingACarSuccessfully() throws Exception {
    // Initialize a parking request
    var parkingRequest = new ParkingRequest();
    parkingRequest.setCarId("11-AA");
    parkingRequest.setCarHeight(new BigDecimal("160.00"));
    parkingRequest.setCarWeight(new BigDecimal("1500.00"));

    // Send a POST request to park the car and verify the response
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest)))
        .andDo(print())
        .andExpect(status().isOk()) // No failure. Successfully parked.
        .andExpect(jsonPath("$").doesNotExist()) // Because it's void
        .andReturn();

    // The ceiling height of the floor 1: 285 cm
    // The ceiling height of the floor 2: 130 cm
    // The ceiling height of the floor 3: 170 cm
    // The height of the car: 160 cm
    // All weight capacities are already far higher than the car's weight at the beginning.
    // In conclusion, the best floor is 3. (the closest ceiling height)

    // Verify created parking record
    var parkingRecord = parkingRecordRepository.findOngoingParkingRecordByCarId(
        parkingRequest.getCarId());
    assertNotNull(parkingRecord);
    assertEquals(parkingRequest.getCarId(), parkingRecord.getCarId());
    assertEquals(parkingRequest.getCarHeight(), parkingRecord.getCarHeight());
    assertEquals(parkingRequest.getCarWeight(), parkingRecord.getCarWeight());
    assertEquals(3, parkingRecord.getFloor());
    assertEquals(new BigDecimal("20000.00"), parkingRecord.getAllowedWeightOnFloorBeforeParking());
    assertEquals(ParkingStatus.PARKING_IN_PROGRESS.name(), parkingRecord.getStatus());
    assertNotNull(parkingRecord.getParkingTimestamp());
    assertNull(parkingRecord.getEmptyingTimestamp());

    // Verify floor after the car is parked on it
    var floor = floorRepository.findFloorByFloorNumber(parkingRecord.getFloor());
    assertNotEquals(floor.getAllowedWeight(), floor.getWeightCapacity());
    assertEquals(new BigDecimal("18500.00"), floor.getAllowedWeight()); // 20000 - 1500 = 18500

    // Verify parking space
    var parkingSpace = parkingSpaceRepository.findById(parkingRecord.getParkingSpaceId())
        .orElseThrow();
    assertEquals(parkingRequest.getCarId(), parkingSpace.getOccupyingCarId());
  }

  /**
   * The execution of the following test takes slightly more than 2 minutes. It's basically a
   * simulation of waiting for the second car leaving the parking lot. Ultimately the total price to
   * be paid in the bill for the second car will be 2 times price-per-minute rate.
   */
  @Test
  void test2_parkTwoCars_emptySecondParkingSpaceAfter2MinutesAndBillSuccessfully()
      throws Exception {
    // Let's park 2 cars in this case.
    // Park the first car
    var parkingRequest1 = new ParkingRequest();
    parkingRequest1.setCarId("22-BB");
    parkingRequest1.setCarHeight(new BigDecimal("160.00"));
    parkingRequest1.setCarWeight(new BigDecimal("1500.00"));
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest1)))
        .andDo(print())
        .andExpect(status().isOk()) // No failure. Successfully parked.
        .andExpect(jsonPath("$").doesNotExist()) // Because it's void
        .andReturn();

    // Park the second car
    var parkingRequest2 = new ParkingRequest();
    parkingRequest2.setCarId("22-CC");
    parkingRequest2.setCarHeight(new BigDecimal("150.00"));
    parkingRequest2.setCarWeight(new BigDecimal("1400.00"));
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest2)))
        .andDo(print())
        .andExpect(status().isOk()) // No failure. Successfully parked.
        .andExpect(jsonPath("$").doesNotExist()) // Because it's void
        .andReturn();
    var parkingRecordForSecondCar = parkingRecordRepository.findOngoingParkingRecordByCarId(
        parkingRequest2.getCarId());

    // The first car stays in the parking lot. Let's pull the second car out of the parking lot.

    // Simulate waiting for 2 minutes before the second car leaving the parking lot
    latch.await(2, TimeUnit.MINUTES);

    // Send a POST request to empty the parking space occupied by the second car
    mockMvc.perform(post("/automated-parking-lot/pull-out-and-bill/" + parkingRequest2.getCarId())
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk()) // No failure. The car has successfully left the parking lot.
        .andExpect(jsonPath("$").doesNotExist()) // Because it's void
        .andReturn();

    // Verify the parking record for the second car has been updated
    var updatedParkingRecordForSecondCar = parkingRecordRepository.findById(
            parkingRecordForSecondCar.getId())
        .orElseThrow();
    assertNotNull(updatedParkingRecordForSecondCar.getEmptyingTimestamp());
    assertEquals(ParkingStatus.PARKING_OVER.name(), updatedParkingRecordForSecondCar.getStatus());

    // Verify the floor after emptying the parking space occupied by the second car
    var floor = floorRepository.findFloorByFloorNumber(parkingRecordForSecondCar.getFloor());
    assertEquals(new BigDecimal("18500.00"),
        floor.getAllowedWeight()); // 20000 - 1500 - 1400 + 1400 = 18500

    // Verify the parking space that was occupied by the second car is now empty
    var parkingSpace = parkingSpaceRepository.findById(
            parkingRecordForSecondCar.getParkingSpaceId())
        .orElseThrow();
    assertNull(parkingSpace.getOccupyingCarId());

    // Expected price-per-minute calculation for the second car:
    // Allowed weight on the floor before parking / floor's weight capacity = 18500 / 20000 ~ 0.92
    // Price-per-minute ~ 0.92

    // Verify the parking bill
    var bills = billRepository.findAll();
    assertNotNull(bills);
    var onlyBillFromRecentlyCompletedParking = bills.iterator().next();
    assertEquals(onlyBillFromRecentlyCompletedParking.getCarId(), parkingRequest2.getCarId());
    assertEquals(onlyBillFromRecentlyCompletedParking.getPricePerMinute(), new BigDecimal("0.92"));
    assertEquals(onlyBillFromRecentlyCompletedParking.getTotalAmountToBePaid(),
        new BigDecimal("1.84")); // 0.92 * 2 = 1.84
    assertEquals(onlyBillFromRecentlyCompletedParking.getBillingFrom(),
        updatedParkingRecordForSecondCar.getParkingTimestamp().format(
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    assertEquals(onlyBillFromRecentlyCompletedParking.getBillingTo(),
        updatedParkingRecordForSecondCar.getEmptyingTimestamp().format(
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
  }

  @Test
  void test3_mandatoryCarWeightFieldMissing_parkingCarFailed() throws Exception {
    // Initialize a parking request, but leave the weight info null
    var parkingRequest = new ParkingRequest();
    parkingRequest.setCarId("33-DD");
    parkingRequest.setCarHeight(new BigDecimal("192.00"));

    // Send a POST request to park the car and verify the response is failure
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest()) // Bad request, because an exception has been thrown.
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(
            AutomatedParkingExceptionCode.CAR_WEIGHT_MISSING.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            AutomatedParkingExceptionCode.CAR_WEIGHT_MISSING.getExplanatoryMessage()));
  }

  @Test
  void test4_mandatoryCarHeightFieldMissing_parkingCarFailed() throws Exception {
    // Initialize a parking request, but leave the height info null
    var parkingRequest = new ParkingRequest();
    parkingRequest.setCarId("44-EE");
    parkingRequest.setCarWeight(new BigDecimal("1800.00"));

    // Send a POST request to park the car and verify the response is failure
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest()) // Bad request, because an exception has been thrown.
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(
            AutomatedParkingExceptionCode.CAR_HEIGHT_MISSING.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            AutomatedParkingExceptionCode.CAR_HEIGHT_MISSING.getExplanatoryMessage()));
  }

  @Test
  void test5_mandatoryCarIdFieldMissing_parkingCarFailed() throws Exception {
    // Initialize a parking request, but leave the car ID info null
    var parkingRequest = new ParkingRequest();
    parkingRequest.setCarHeight(new BigDecimal("185.00"));
    parkingRequest.setCarWeight(new BigDecimal("1800.00"));

    // Send a POST request to park the car and verify the response is failure
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest()) // Bad request, because an exception has been thrown.
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(
            AutomatedParkingExceptionCode.CAR_ID_MISSING.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            AutomatedParkingExceptionCode.CAR_ID_MISSING.getExplanatoryMessage()));
  }

  @Test
  void test6_alreadyParkedCarIdAttemptedToBeParkedAgain_parkingCarFailed() throws Exception {
    // Park a car with the ID 66-GG
    var parkingRequest1 = new ParkingRequest();
    parkingRequest1.setCarId("66-GG");
    parkingRequest1.setCarHeight(new BigDecimal("160.00"));
    parkingRequest1.setCarWeight(new BigDecimal("1500.00"));
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest1)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").doesNotExist())
        .andReturn();

    // Try to park another car with the ID 66-GG again
    // Park the first car
    var parkingRequest2 = new ParkingRequest();
    parkingRequest2.setCarId("66-GG");
    parkingRequest2.setCarHeight(new BigDecimal("178.00"));
    parkingRequest2.setCarWeight(new BigDecimal("1360.00"));
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest2)))
        .andDo(print())
        .andExpect(status().isBadRequest()) // Bad request, because an exception has been thrown.
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(
            AutomatedParkingExceptionCode.CAR_ALREADY_PARKED.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            AutomatedParkingExceptionCode.CAR_ALREADY_PARKED.getExplanatoryMessage()));
  }

  @Test
  void test7_wrongIdOfCarToLeaveParkingLot_leavingParkingLotFailed() throws Exception {
    // Park a car with the ID 77-HH
    var parkingRequest = new ParkingRequest();
    parkingRequest.setCarId("77-HH");
    parkingRequest.setCarHeight(new BigDecimal("150.00"));
    parkingRequest.setCarWeight(new BigDecimal("1270.00"));
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").doesNotExist())
        .andReturn();

    // Try to pull the car with another unknown ID out of the parking lot
    mockMvc.perform(post("/automated-parking-lot/pull-out-and-bill/77-XX")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest()) // Bad request, because an exception has been thrown.
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(
            AutomatedParkingExceptionCode.NO_PARKED_CAR_WITH_THIS_ID.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            AutomatedParkingExceptionCode.NO_PARKED_CAR_WITH_THIS_ID.getExplanatoryMessage()));
  }

  @Test
  void test8_noAvailableFloorDueToCeilingHeight_parkingCarFailed() throws Exception {
    // Initialize a parking request, but the car is too high to fit the floors of the parking lot
    var parkingRequest = new ParkingRequest();
    parkingRequest.setCarId("88-II");
    parkingRequest.setCarHeight(new BigDecimal("200.00"));
    parkingRequest.setCarWeight(new BigDecimal("1750.00"));

    // 200 > all floor ceiling heights

    // Send a POST request to park the car and verify the response is failure
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest()) // Bad request, because an exception has been thrown.
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(
            AutomatedParkingExceptionCode.NO_AVAILABLE_FLOOR.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            AutomatedParkingExceptionCode.NO_AVAILABLE_FLOOR.getExplanatoryMessage()));
  }

  @Test
  void test9_noAvailableFloorAsCarTooHeavy_parkingCarFailed() throws Exception {
    // Assume that there are already cars parked on all floors.
    var floor1 = floorRepository.findFloorByFloorNumber(1);
    floor1.setAllowedWeight(new BigDecimal("1200.00"));
    var floor2 = floorRepository.findFloorByFloorNumber(2);
    floor2.setAllowedWeight(new BigDecimal("890.00"));
    var floor3 = floorRepository.findFloorByFloorNumber(3);
    floor3.setAllowedWeight(new BigDecimal("1076.00"));
    floorRepository.saveAll(List.of(floor1, floor2, floor3));

    // Initialize a parking request, but the car is too heavy to fit the floors of the parking lot
    var parkingRequest = new ParkingRequest();
    parkingRequest.setCarId("99-JJ");
    parkingRequest.setCarHeight(new BigDecimal("168.00"));
    parkingRequest.setCarWeight(new BigDecimal("1300.00"));

    // 1300 kg > all allowed weights

    // Send a POST request to park the car and verify the response is failure
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest()) // Bad request, because an exception has been thrown.
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(
            AutomatedParkingExceptionCode.NO_AVAILABLE_FLOOR.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            AutomatedParkingExceptionCode.NO_AVAILABLE_FLOOR.getExplanatoryMessage()));
  }

  @Test
  void test10_noAvailableFloorAsParkingSpacesOnFittingFloorOccupied_parkingCarFailed() throws Exception {
    // Initialize a parking request
    var parkingRequest = new ParkingRequest();
    parkingRequest.setCarId("100-KK");
    parkingRequest.setCarHeight(new BigDecimal("185.00"));
    parkingRequest.setCarWeight(new BigDecimal("1300.00"));

    // According to the floors table, the only fitting floor is floor 1 because of sufficient ceiling height.
    // 185 cm > 130 cm, 185 cm > 170 cm, 185 cm < 195 cm
    // However, let's fill all parking spaces on this floor.
    var parkingSpacesOnFittingFloor = parkingSpaceRepository.findAvailableParkingSpacesOnFloor(1);
    for (int i=0; i<parkingSpacesOnFittingFloor.size(); i++) {
      parkingSpacesOnFittingFloor.get(i).setOccupyingCarId(String.valueOf(i));
    }
    parkingSpaceRepository.saveAll(parkingSpacesOnFittingFloor);

    // Send a POST request to park the car and verify the response is failure
    mockMvc.perform(post("/automated-parking-lot/park")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(parkingRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest()) // Bad request, because an exception has been thrown.
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode").value(
            AutomatedParkingExceptionCode.NO_AVAILABLE_FLOOR.name()))
        .andExpect(jsonPath("$.errorMessage").value(
            AutomatedParkingExceptionCode.NO_AVAILABLE_FLOOR.getExplanatoryMessage()));
  }

}
