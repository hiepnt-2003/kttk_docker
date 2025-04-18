// client/CustomerClient.java
package com.rentalmanagement.paymentservice.client;

import com.rentalmanagement.paymentservice.dto.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerClient {
    @GetMapping("/api/customers/{id}")
    ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id);
}