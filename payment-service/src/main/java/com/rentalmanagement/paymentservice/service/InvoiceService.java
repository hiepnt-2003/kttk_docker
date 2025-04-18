// service/InvoiceService.java
package com.rentalmanagement.paymentservice.service;

import com.rentalmanagement.paymentservice.client.CustomerClient;
import com.rentalmanagement.paymentservice.client.RoomClient;
import com.rentalmanagement.paymentservice.dto.CustomerDto;
import com.rentalmanagement.paymentservice.dto.InvoiceDto;
import com.rentalmanagement.paymentservice.dto.RoomDto;
import com.rentalmanagement.paymentservice.exception.ResourceNotFoundException;
import com.rentalmanagement.paymentservice.model.Invoice;
import com.rentalmanagement.paymentservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CustomerClient customerClient;
    private final RoomClient roomClient;

    public List<InvoiceDto> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + id));
        return convertToDto(invoice);
    }

    public InvoiceDto getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với số: " + invoiceNumber));
        return convertToDto(invoice);
    }

    public List<InvoiceDto> getInvoicesByCustomerId(Long customerId) {
        return invoiceRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<InvoiceDto> getInvoicesByRoomId(Long roomId) {
        return invoiceRepository.findByRoomId(roomId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<InvoiceDto> getInvoicesByStatus(Invoice.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<InvoiceDto> getInvoicesByMonth(String monthYear) {
        return invoiceRepository.findByMonthYear(monthYear).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<InvoiceDto> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<InvoiceDto> searchInvoices(String term) {
        return invoiceRepository.searchInvoices(term).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        // Kiểm tra khách hàng
        CustomerDto customer = customerClient.getCustomerById(invoiceDto.getCustomerId()).getBody();
        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + invoiceDto.getCustomerId());
        }

        // Kiểm tra phòng
        RoomDto room = roomClient.getRoomById(invoiceDto.getRoomId()).getBody();
        if (room == null) {
            throw new ResourceNotFoundException("Không tìm thấy phòng với ID: " + invoiceDto.getRoomId());
        }

        // Kiểm tra xem đã có hóa đơn cho phòng và tháng này chưa
        List<Invoice> existingInvoices = invoiceRepository.findPendingInvoicesByCustomerAndRoomAndMonth(
                invoiceDto.getCustomerId(), invoiceDto.getRoomId(), invoiceDto.getMonthYear());

        if (!existingInvoices.isEmpty()) {
            throw new IllegalStateException("Đã tồn tại hóa đơn chưa thanh toán cho phòng này trong tháng " + invoiceDto.getMonthYear());
        }

        // Tạo số hóa đơn
        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = convertToEntity(invoiceDto);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setIssueDate(LocalDateTime.now());
        invoice.setStatus(Invoice.InvoiceStatus.PENDING);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return convertToDto(savedInvoice);
    }

    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + id));

        // Không cho phép cập nhật hóa đơn đã thanh toán
        if (existingInvoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Không thể cập nhật hóa đơn đã thanh toán");
        }

        Invoice invoice = convertToEntity(invoiceDto);
        invoice.setId(id);
        invoice.setInvoiceNumber(existingInvoice.getInvoiceNumber());
        invoice.setIssueDate(existingInvoice.getIssueDate());
        invoice.setCreatedAt(existingInvoice.getCreatedAt());

        if (invoice.getStatus() == null) {
            invoice.setStatus(existingInvoice.getStatus());
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return convertToDto(updatedInvoice);
    }

    public InvoiceDto updateInvoiceStatus(Long id, Invoice.InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + id));

        invoice.setStatus(status);
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return convertToDto(updatedInvoice);
    }

    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + id));

        // Không cho phép xóa hóa đơn đã thanh toán
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Không thể xóa hóa đơn đã thanh toán");
        }

        invoiceRepository.delete(invoice);
    }

    private String generateInvoiceNumber() {
        // Tạo số hóa đơn với định dạng INV-yyyyMMdd-XXXX (X là ký tự ngẫu nhiên)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = LocalDate.now().format(formatter);
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return "INV-" + datePart + "-" + randomPart;
    }

    public InvoiceDto convertToDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCustomerId(invoice.getCustomerId());
        dto.setRoomId(invoice.getRoomId());
        dto.setMonthYear(invoice.getMonthYear());
        dto.setAmount(invoice.getAmount());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus());
        dto.setDescription(invoice.getDescription());

        // Lấy thông tin chi tiết khách hàng và phòng
        try {
            CustomerDto customer = customerClient.getCustomerById(invoice.getCustomerId()).getBody();
            if (customer != null) {
                dto.setCustomerName(customer.getFullName());
            }

            RoomDto room = roomClient.getRoomById(invoice.getRoomId()).getBody();
            if (room != null) {
                dto.setRoomNumber(room.getRoomNumber());
            }
        } catch (Exception e) {
            // Xử lý khi dịch vụ khác không khả dụng
        }

        return dto;
    }

    private Invoice convertToEntity(InvoiceDto invoiceDto) {
        Invoice invoice = new Invoice();
        invoice.setId(invoiceDto.getId());
        invoice.setInvoiceNumber(invoiceDto.getInvoiceNumber());
        invoice.setCustomerId(invoiceDto.getCustomerId());
        invoice.setRoomId(invoiceDto.getRoomId());
        invoice.setMonthYear(invoiceDto.getMonthYear());
        invoice.setAmount(invoiceDto.getAmount());
        invoice.setDueDate(invoiceDto.getDueDate());

        if (invoiceDto.getStatus() != null) {
            invoice.setStatus(invoiceDto.getStatus());
        }

        invoice.setDescription(invoiceDto.getDescription());
        return invoice;
    }
}