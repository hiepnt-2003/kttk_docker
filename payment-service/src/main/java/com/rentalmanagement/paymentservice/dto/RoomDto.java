// dto/RoomDto.java
package com.rentalmanagement.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private String roomNumber;
    private Long roomTypeId;
    private String roomTypeName;
    private BigDecimal monthlyPrice;
    private String status;
    private String description;
}