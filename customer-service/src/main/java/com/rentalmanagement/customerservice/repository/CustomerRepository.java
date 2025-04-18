// repository/CustomerRepository.java
package com.rentalmanagement.customerservice.repository;

import com.rentalmanagement.customerservice.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdentificationNumber(String identificationNumber);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    @Query("SELECT c FROM Customer c WHERE c.identificationNumber = ?1 OR c.phoneNumber = ?1")
    List<Customer> searchByIdentificationOrPhone(String searchTerm);
}