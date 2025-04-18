// controller/ReportController.java
package com.rentalmanagement.paymentservice.controller;

import com.rentalmanagement.paymentservice.dto.ReportDto;
import com.rentalmanagement.paymentservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/monthly")
    public ResponseEntity<ReportDto> getMonthlyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateMonthlyReport(startDate, endDate));
    }

    @GetMapping("/quarterly")
    public ResponseEntity<ReportDto> getQuarterlyReport(
            @RequestParam int year,
            @RequestParam(required = false, defaultValue = "0") int quarter) {
        return ResponseEntity.ok(reportService.generateQuarterlyReport(year, quarter));
    }

    @GetMapping("/yearly")
    public ResponseEntity<ReportDto> getYearlyReport(@RequestParam int year) {
        return ResponseEntity.ok(reportService.generateYearlyReport(year));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ReportDto> getCustomerReport(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateCustomerReport(customerId, startDate, endDate));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ReportDto> getRoomReport(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateRoomReport(roomId, startDate, endDate));
    }
}