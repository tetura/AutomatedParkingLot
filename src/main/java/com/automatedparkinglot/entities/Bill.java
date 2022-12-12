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
 * An entity covering a bill to be created after parking
 */
@Data
@Entity
@Table(name = "bills")
public class Bill {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotNull
  private String carId;
  @NotNull
  private String billingFrom;
  @NotNull
  private String billingTo;
  @NotNull
  @Min(0)
  private BigDecimal pricePerMinute;
  @NotNull
  @Min(0)
  private BigDecimal totalAmountToBePaid;

}
