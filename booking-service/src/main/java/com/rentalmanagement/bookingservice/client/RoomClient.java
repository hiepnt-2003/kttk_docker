// client/RoomClient.java
package com.rentalmanagement.bookingservice.client;

import com.rentalmanagement.bookingservice.dto.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "room-service")
public interface RoomClient {
    @GetMapping("/api/rooms/{id}")
    ResponseEntity<RoomDto> getRoomById(@PathVariable Long id);

    @GetMapping("/api/rooms/available")
    ResponseEntity<List<RoomDto>> getAvailableRooms();

    @PatchMapping("/api/rooms/{id}/status")
    ResponseEntity<RoomDto> updateRoomStatus(@PathVariable Long id, @RequestBody String status);
}