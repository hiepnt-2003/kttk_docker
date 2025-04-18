// client/CustomerClient.java
package com.rentalmanagement.bookingservice.client;

import com.rentalmanagement.bookingservice.dto.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "customer-service")
public interface CustomerClient {
    @GetMapping("/api/customers/{id}")
    ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id);

    @GetMapping("/api/customers/search")
    ResponseEntity<List<CustomerDto>> searchCustomers(@RequestParam String term);
}