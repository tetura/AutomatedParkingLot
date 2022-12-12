package com.automatedparkinglot.repositories;

import com.automatedparkinglot.entities.Bill;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A CrudRepository to handle database operations of parking bills
 */
@Repository
public interface BillRepository extends CrudRepository<Bill, Long> {

}
