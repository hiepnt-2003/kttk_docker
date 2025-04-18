// controller/PaymentController.java
package com.rentalmanagement.paymentservice.controller;

import com.rentalmanagement.paymentservice.dto.PaymentDto;
import com.rentalmanagement.paymentservice.model.Payment;
import com.rentalmanagement.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByInvoiceId(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getPaymentsByInvoiceId(invoiceId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomerId(customerId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByRoomId(@PathVariable Long roomId) {
        return ResponseEntity.ok(paymentService.getPaymentsByRoomId(roomId));
    }

    @GetMapping("/method/{paymentMethod}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByMethod(@PathVariable Payment.PaymentMethod paymentMethod) {
        return ResponseEntity.ok(paymentService.getPaymentsByMethod(paymentMethod));
    }

    @GetMapping("/month/{monthYear}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByMonth(@PathVariable String monthYear) {
        return ResponseEntity.ok(paymentService.getPaymentsByMonth(monthYear));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<PaymentDto>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(paymentService.getPaymentsByDateRange(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@Valid @RequestBody PaymentDto paymentDto) {
        return new ResponseEntity<>(paymentService.createPayment(paymentDto), HttpStatus.CREATED);
    }

    @PostMapping("/pay-invoice/{invoiceId}")
    public ResponseEntity<PaymentDto> payInvoice(
            @PathVariable Long invoiceId,
            @RequestParam Payment.PaymentMethod paymentMethod,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String notes) {
        return new ResponseEntity<>(paymentService.payInvoice(invoiceId, paymentMethod, transactionId, notes), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentDto> updatePayment(@PathVariable Long id, @Valid @RequestBody PaymentDto paymentDto) {
        return ResponseEntity.ok(paymentService.updatePayment(id, paymentDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}