// controller/CheckInController.java (tiếp)
package com.rentalmanagement.bookingservice.controller;

import com.rentalmanagement.bookingservice.dto.CheckInDto;
import com.rentalmanagement.bookingservice.model.CheckIn;
import com.rentalmanagement.bookingservice.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/check-ins")
@RequiredArgsConstructor
public class CheckInController {
    private final CheckInService checkInService;

    @GetMapping
    public ResponseEntity<List<CheckInDto>> getAllCheckIns() {
        return ResponseEntity.ok(checkInService.getAllCheckIns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckInDto> getCheckInById(@PathVariable Long id) {
        return ResponseEntity.ok(checkInService.getCheckInById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CheckInDto>> getCheckInsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(checkInService.getCheckInsByCustomerId(customerId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<CheckInDto>> getCheckInsByRoomId(@PathVariable Long roomId) {
        return ResponseEntity.ok(checkInService.getCheckInsByRoomId(roomId));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<CheckInDto> getCheckInByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(checkInService.getCheckInByBookingId(bookingId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CheckInDto>> getCheckInsByStatus(@PathVariable CheckIn.CheckInStatus status) {
        return ResponseEntity.ok(checkInService.getCheckInsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<CheckInDto> createCheckIn(@Valid @RequestBody CheckInDto checkInDto) {
        return new ResponseEntity<>(checkInService.createCheckIn(checkInDto), HttpStatus.CREATED);
    }

    @PostMapping("/from-booking/{bookingId}")
    public ResponseEntity<CheckInDto> createCheckInFromBooking(@PathVariable Long bookingId) {
        return new ResponseEntity<>(checkInService.createCheckInFromBooking(bookingId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CheckInDto> updateCheckIn(@PathVariable Long id, @Valid @RequestBody CheckInDto checkInDto) {
        return ResponseEntity.ok(checkInService.updateCheckIn(id, checkInDto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CheckInDto> updateCheckInStatus(@PathVariable Long id, @RequestBody CheckIn.CheckInStatus status) {
        return ResponseEntity.ok(checkInService.updateCheckInStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheckIn(@PathVariable Long id) {
        checkInService.deleteCheckIn(id);
        return ResponseEntity.noContent().build();
    }
}