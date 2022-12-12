package com.automatedparkinglot.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * An entity covering the record of a parking that covers some information about parking itself and
 * the parked car
 */
@Data
@Entity
@Table(name = "parking_records")
public class ParkingRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotNull
  private String carId;
  @NotNull
  private BigDecimal carWeight;
  @NotNull
  private BigDecimal carHeight;
  @NotNull
  private BigDecimal allowedWeightOnFloorBeforeParking;
  @NotNull
  private LocalDateTime parkingTimestamp;
  private LocalDateTime emptyingTimestamp;
  @NotNull
  private Integer floor;
  @NotNull
  private Long parkingSpaceId;
  @NotNull
  private String status;

}
