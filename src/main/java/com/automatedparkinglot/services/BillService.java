package com.automatedparkinglot.services;

import com.automatedparkinglot.entities.Bill;
import com.automatedparkinglot.entities.Floor;
import com.automatedparkinglot.entities.ParkingRecord;
import com.automatedparkinglot.repositories.BillRepository;
import com.automatedparkinglot.util.PlaceholderPrinter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service to handle pricing and bill generation
 */
@Service
public class BillService {

  private final BillRepository billRepository;

  @Autowired
  public BillService(BillRepository billRepository) {
    this.billRepository = billRepository;
  }

  /**
   * Generates a parking bill after parking is completed
   *
   * @param parkingRecord Parking for which a bill is to be generated
   * @param floor         The floor where the car was parking, which is used to calculate price
   */
  @Transactional
  public void generateBill(ParkingRecord parkingRecord, Floor floor) {
    PlaceholderPrinter.printMovementInformation(
        String.format(
            "The parking of the car %s in the parking space %s on the floor %s is over and the parking data are being transferred to the billing system.",
            parkingRecord.getCarId(), parkingRecord.getParkingSpaceId(), parkingRecord.getFloor()));
    var pricePerMinute = this.calculatePricePerMinute(parkingRecord, floor);
    var occupationDurationInMinutes = new BigDecimal(
        ChronoUnit.SECONDS.between(parkingRecord.getParkingTimestamp(),
            parkingRecord.getEmptyingTimestamp()) / 60).setScale(0, RoundingMode.DOWN);
    var totalAmountToBePaid = occupationDurationInMinutes.multiply(pricePerMinute)
        .setScale(2, RoundingMode.DOWN);
    var bill = new Bill();
    bill.setCarId(parkingRecord.getCarId());
    bill.setBillingFrom(parkingRecord.getParkingTimestamp()
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    bill.setBillingTo(parkingRecord.getEmptyingTimestamp()
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    bill.setPricePerMinute(pricePerMinute);
    bill.setTotalAmountToBePaid(totalAmountToBePaid);
    billRepository.save(bill);
    PlaceholderPrinter.printBill(bill);
  }

  /**
   * Calculates a price-per-minute rate based on demand on the floor
   *
   * @param parkingRecord Parking for which a price-per-minute rate is to be calculated
   * @param floor         The floor where the car was parking
   * @return Calculated price-per-minute
   */
  private BigDecimal calculatePricePerMinute(ParkingRecord parkingRecord, Floor floor) {
    return BigDecimal.valueOf(parkingRecord.getAllowedWeightOnFloorBeforeParking().doubleValue()
        / floor.getWeightCapacity().doubleValue()).setScale(2, RoundingMode.DOWN);
  }

}
