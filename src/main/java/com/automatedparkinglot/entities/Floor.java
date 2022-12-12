package com.automatedparkinglot.entities;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * An entity covering a floor of the parking lot: floor's ID, floor's ordinal number from ground,
 * floor's ceiling height, floor's maximum weight capacity, floor's remaining weight allowed for new
 * cars to be parked
 */
@Data
@Entity
@Table(name = "floors")
public class Floor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotNull
  private Integer number;
  @NotNull
  private BigDecimal ceilingHeight;
  @NotNull
  private BigDecimal weightCapacity;
  @NotNull
  @Min(0)
  private BigDecimal allowedWeight;

}
