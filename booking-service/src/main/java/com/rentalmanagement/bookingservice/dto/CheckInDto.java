// dto/CheckInDto.java
package com.rentalmanagement.bookingservice.dto;

import com.rentalmanagement.bookingservice.model.CheckIn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInDto {
    private Long id;

    private Long bookingId;

    @NotNull(message = "ID khách hàng không được để trống")
    private Long customerId;

    private String customerName;

    @NotNull(message = "ID phòng không được để trống")
    private Long roomId;

    private String roomNumber;

    @NotNull(message = "Ngày nhận phòng không được để trống")
    private LocalDate checkInDate;

    private LocalDate expectedCheckOutDate;

    private CheckIn.CheckInStatus status;
}