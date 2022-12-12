package com.automatedparkinglot.util;

import com.automatedparkinglot.entities.Bill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaceholderPrinter {

  private static final String EQUAL_SIGNS_FRAME_LINE = "===================================================";
  private static final Logger logger = LoggerFactory.getLogger(PlaceholderPrinter.class);

  private PlaceholderPrinter() {
  }

  public static void printMovementInformation(String informationToBePrinted) {
    logger.info("\n\n{}\n{}{}\n{}\n\n", EQUAL_SIGNS_FRAME_LINE, ">> ", informationToBePrinted,
        EQUAL_SIGNS_FRAME_LINE);
  }

  public static void printBill(Bill bill) {
    var aligningFormat = "%-25s %25s %n";
    String billPrinterBuffer = "\n\n" + EQUAL_SIGNS_FRAME_LINE + "\n"
        + "================= PARKING BILL ====================\n"
        + String.format(aligningFormat, "Car ID", bill.getCarId())
        + String.format(aligningFormat, "Parking started at", bill.getBillingFrom())
        + String.format(aligningFormat, "Parking ended at", bill.getBillingTo())
        + String.format(aligningFormat, "Price-per-minute", bill.getPricePerMinute() + "€")
        + String.format(aligningFormat, "PARKING FEE", bill.getTotalAmountToBePaid() + "€")
        + EQUAL_SIGNS_FRAME_LINE;
    logger.info(billPrinterBuffer);
  }

}
