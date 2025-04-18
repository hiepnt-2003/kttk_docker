// dto/BookingDto.java
package com.rentalmanagement.bookingservice.dto;

import com.rentalmanagement.bookingservice.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;

    @NotNull(message = "ID khách hàng không được để trống")
    private Long customerId;

    private String customerName;

    @NotNull(message = "ID phòng không được để trống")
    private Long roomId;

    private String roomNumber;

    @NotNull(message = "Ngày nhận phòng không được để trống")
    @Future(message = "Ngày nhận phòng phải là ngày trong tương lai")
    private LocalDate checkInDate;

    private Booking.BookingStatus status;

    private String notes;
}