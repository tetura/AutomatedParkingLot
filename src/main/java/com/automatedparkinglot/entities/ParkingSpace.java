package com.automatedparkinglot.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * An entity covering a parking space on a floor: parking space ID, the ID of the car occupying the
 * parking space (if the parking space is empty, this is null), and the number of the floor parking
 * space belongs to
 */
@Data
@Entity
@Table(name = "parking_spaces")
public class ParkingSpace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String occupyingCarId;
  @NotNull
  private Integer floor;
}
