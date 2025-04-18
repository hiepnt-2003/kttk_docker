// service/PaymentService.java (tiếp)
package com.rentalmanagement.paymentservice.service;

import com.rentalmanagement.paymentservice.client.CustomerClient;
import com.rentalmanagement.paymentservice.client.RoomClient;
import com.rentalmanagement.paymentservice.dto.CustomerDto;
import com.rentalmanagement.paymentservice.dto.PaymentDto;
import com.rentalmanagement.paymentservice.dto.RoomDto;
import com.rentalmanagement.paymentservice.exception.ResourceNotFoundException;
import com.rentalmanagement.paymentservice.model.Invoice;
import com.rentalmanagement.paymentservice.model.Payment;
import com.rentalmanagement.paymentservice.repository.InvoiceRepository;
import com.rentalmanagement.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerClient customerClient;
    private final RoomClient roomClient;

    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thanh toán với ID: " + id));
        return convertToDto(payment);
    }

    public List<PaymentDto> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsByCustomerId(Long customerId) {
        return paymentRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsByRoomId(Long roomId) {
        return paymentRepository.findByRoomId(roomId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsByMethod(Payment.PaymentMethod paymentMethod) {
        return paymentRepository.findByPaymentMethod(paymentMethod).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsByMonth(String monthYear) {
        return paymentRepository.findByMonthYear(monthYear).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PaymentDto createPayment(PaymentDto paymentDto) {
        // Kiểm tra khách hàng
        CustomerDto customer = customerClient.getCustomerById(paymentDto.getCustomerId()).getBody();
        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + paymentDto.getCustomerId());
        }

        // Kiểm tra phòng
        RoomDto room = roomClient.getRoomById(paymentDto.getRoomId()).getBody();
        if (room == null) {
            throw new ResourceNotFoundException("Không tìm thấy phòng với ID: " + paymentDto.getRoomId());
        }

        // Kiểm tra hóa đơn nếu có
        if (paymentDto.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findById(paymentDto.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + paymentDto.getInvoiceId()));

            // Cập nhật trạng thái hóa đơn thành đã thanh toán
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            invoiceRepository.save(invoice);
        }

        Payment payment = convertToEntity(paymentDto);
        payment.setPaymentDate(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        return convertToDto(savedPayment);
    }

    @Transactional
    public PaymentDto payInvoice(Long invoiceId, Payment.PaymentMethod paymentMethod, String transactionId, String notes) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Hóa đơn này đã được thanh toán");
        }

        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Không thể thanh toán hóa đơn đã hủy");
        }

        // Tạo thanh toán mới
        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setCustomerId(invoice.getCustomerId());
        payment.setRoomId(invoice.getRoomId());
        payment.setAmount(invoice.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId(transactionId);
        payment.setMonthYear(invoice.getMonthYear());
        payment.setNotes(notes);

        Payment savedPayment = paymentRepository.save(payment);

        // Cập nhật trạng thái hóa đơn
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        return convertToDto(savedPayment);
    }

    public PaymentDto updatePayment(Long id, PaymentDto paymentDto) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thanh toán với ID: " + id));

        Payment payment = convertToEntity(paymentDto);
        payment.setId(id);
        payment.setCreatedAt(existingPayment.getCreatedAt());

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDto(updatedPayment);
    }

    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thanh toán với ID: " + id));

        // Cập nhật lại trạng thái hóa đơn nếu có
        if (payment.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findById(payment.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + payment.getInvoiceId()));

            invoice.setStatus(Invoice.InvoiceStatus.PENDING);
            invoiceRepository.save(invoice);
        }

        paymentRepository.delete(payment);
    }

    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setInvoiceId(payment.getInvoiceId());
        dto.setCustomerId(payment.getCustomerId());
        dto.setRoomId(payment.getRoomId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setTransactionId(payment.getTransactionId());
        dto.setMonthYear(payment.getMonthYear());
        dto.setNotes(payment.getNotes());

        // Lấy thông tin chi tiết khách hàng và phòng
        try {
            CustomerDto customer = customerClient.getCustomerById(payment.getCustomerId()).getBody();
            if (customer != null) {
                dto.setCustomerName(customer.getFullName());
            }

            RoomDto room = roomClient.getRoomById(payment.getRoomId()).getBody();
            if (room != null) {
                dto.setRoomNumber(room.getRoomNumber());
            }

            // Lấy số hóa đơn nếu có
            if (payment.getInvoiceId() != null) {
                Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
                if (invoice != null) {
                    dto.setInvoiceNumber(invoice.getInvoiceNumber());
                }
            }
        } catch (Exception e) {
            // Xử lý khi dịch vụ khác không khả dụng
        }

        return dto;
    }

    private Payment convertToEntity(PaymentDto paymentDto) {
        Payment payment = new Payment();
        payment.setId(paymentDto.getId());
        payment.setInvoiceId(paymentDto.getInvoiceId());
        payment.setCustomerId(paymentDto.getCustomerId());
        payment.setRoomId(paymentDto.getRoomId());
        payment.setAmount(paymentDto.getAmount());

        if (paymentDto.getPaymentDate() != null) {
            payment.setPaymentDate(paymentDto.getPaymentDate());
        } else {
            payment.setPaymentDate(LocalDateTime.now());
        }

        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setTransactionId(paymentDto.getTransactionId());
        payment.setMonthYear(paymentDto.getMonthYear());
        payment.setNotes(paymentDto.getNotes());

        return payment;
    }
}