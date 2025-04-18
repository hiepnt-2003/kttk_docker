// dto/RoomTypeDto.java
package com.rentalmanagement.roomservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeDto {
    private Long id;

    @NotBlank(message = "Tên loại phòng không được để trống")
    private String name;

    private String description;
}