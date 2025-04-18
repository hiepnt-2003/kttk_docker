// repository/CheckInRepository.java
package com.rentalmanagement.bookingservice.repository;

import com.rentalmanagement.bookingservice.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByCustomerId(Long customerId);

    List<CheckIn> findByRoomId(Long roomId);

    Optional<CheckIn> findByBookingId(Long bookingId);

    List<CheckIn> findByStatus(CheckIn.CheckInStatus status);
}