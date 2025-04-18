// dto/CustomerDto.java
package com.rentalmanagement.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String fullName;
    private String identificationNumber;
    private String phoneNumber;
    private String email;
    private String address;
}