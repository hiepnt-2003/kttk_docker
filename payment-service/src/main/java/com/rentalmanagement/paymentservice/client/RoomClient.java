// client/RoomClient.java
package com.rentalmanagement.paymentservice.client;

import com.rentalmanagement.paymentservice.dto.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "room-service")
public interface RoomClient {
    @GetMapping("/api/rooms/{id}")
    ResponseEntity<RoomDto> getRoomById(@PathVariable Long id);
}