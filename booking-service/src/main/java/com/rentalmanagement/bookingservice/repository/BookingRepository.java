// repository/BookingRepository.java
package com.rentalmanagement.bookingservice.repository;

import com.rentalmanagement.bookingservice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByStatus(Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.customerId = ?1 OR b.id = ?2")
    List<Booking> findByCustomerIdOrBookingId(Long customerId, Long bookingId);
}