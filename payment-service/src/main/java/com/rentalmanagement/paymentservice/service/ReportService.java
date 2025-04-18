// service/ReportService.java
package com.rentalmanagement.paymentservice.service;

import com.rentalmanagement.paymentservice.client.CustomerClient;
import com.rentalmanagement.paymentservice.client.RoomClient;
import com.rentalmanagement.paymentservice.dto.CustomerDto;
import com.rentalmanagement.paymentservice.dto.InvoiceDto;
import com.rentalmanagement.paymentservice.dto.ReportDto;
import com.rentalmanagement.paymentservice.dto.RoomDto;
import com.rentalmanagement.paymentservice.exception.ResourceNotFoundException;
import com.rentalmanagement.paymentservice.model.Invoice;
import com.rentalmanagement.paymentservice.model.Payment;
import com.rentalmanagement.paymentservice.repository.InvoiceRepository;
import com.rentalmanagement.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final CustomerClient customerClient;
    private final RoomClient roomClient;

    public ReportDto generateMonthlyReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);

        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDateTime, endDateTime);
        BigDecimal totalIncome = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy danh sách hóa đơn chưa thanh toán
        List<Invoice> pendingInvoices = invoiceRepository.findByStatus(Invoice.InvoiceStatus.PENDING);
        BigDecimal totalPending = pendingInvoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy danh sách hóa đơn quá hạn
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(LocalDate.now());
        BigDecimal totalOverdue = overdueInvoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Thống kê theo tháng
        Map<String, BigDecimal> incomeByMonth = new HashMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

        for (YearMonth ym = YearMonth.from(startDate); !ym.isAfter(YearMonth.from(endDate)); ym = ym.plusMonths(1)) {
            String monthYear = ym.format(monthFormatter);
            BigDecimal monthlyIncome = payments.stream()
                    .filter(p -> p.getMonthYear().equals(monthYear))
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            incomeByMonth.put(monthYear, monthlyIncome);
        }

        // Thống kê theo phương thức thanh toán
        Map<String, BigDecimal> incomeByPaymentMethod = new HashMap<>();
        for (Payment.PaymentMethod method : Payment.PaymentMethod.values()) {
            BigDecimal methodIncome = payments.stream()
                    .filter(p -> p.getPaymentMethod() == method)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            incomeByPaymentMethod.put(method.name(), methodIncome);
        }

        // Top hóa đơn quá hạn
        List<InvoiceDto> topOverdueInvoices = overdueInvoices.stream()
                .sorted((i1, i2) -> i2.getAmount().compareTo(i1.getAmount()))
                .limit(5)
                .map(invoiceService::convertToDto)
                .collect(Collectors.toList());

        ReportDto reportDto = new ReportDto();
        reportDto.setReportType("MONTHLY");
        reportDto.setPeriod(startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        reportDto.setTotalIncome(totalIncome);
        reportDto.setTotalPending(totalPending);
        reportDto.setTotalOverdue(totalOverdue);
        reportDto.setTotalInvoices(invoiceRepository.findAll().size());
        reportDto.setPaidInvoices(invoiceRepository.findByStatus(Invoice.InvoiceStatus.PAID).size());
        reportDto.setPendingInvoices(pendingInvoices.size());
        reportDto.setOverdueInvoices(overdueInvoices.size());
        reportDto.setIncomeByPeriod(incomeByMonth);
        reportDto.setIncomeByPaymentMethod(incomeByPaymentMethod);
        reportDto.setTopOverdueInvoices(topOverdueInvoices);

        return reportDto;
    }

    public ReportDto generateQuarterlyReport(int year, int quarter) {
        LocalDate startDate;
        LocalDate endDate;

        if (quarter > 0 && quarter <= 4) {
            // Specific quarter
            startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
            endDate = startDate.plusMonths(3).minusDays(1);
        } else {
            // Full year by quarters
            startDate = LocalDate.of(year, 1, 1);
            endDate = LocalDate.of(year, 12, 31);
        }

        ReportDto report = generateMonthlyReport(startDate, endDate);
        report.setReportType("QUARTERLY");

        if (quarter > 0 && quarter <= 4) {
            report.setPeriod("Q" + quarter + "/" + year);
        } else {
            report.setPeriod("All Quarters/" + year);
        }

        return report;
    }

    public ReportDto generateYearlyReport(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        ReportDto report = generateMonthlyReport(startDate, endDate);
        report.setReportType("YEARLY");
        report.setPeriod("Year " + year);

        return report;
    }

    public ReportDto generateCustomerReport(Long customerId, LocalDate startDate, LocalDate endDate) {
        // Kiểm tra khách hàng
        CustomerDto customer = customerClient.getCustomerById(customerId).getBody();
        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);

        List<Payment> payments = paymentRepository.findByCustomerId(customerId).stream()
                .filter(p -> p.getPaymentDate().isAfter(startDateTime) && p.getPaymentDate().isBefore(endDateTime))
                .collect(Collectors.toList());

        BigDecimal totalIncome = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy danh sách hóa đơn chưa thanh toán
        List<Invoice> pendingInvoices = invoiceRepository.findByCustomerId(customerId).stream()
                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PENDING)
                .collect(Collectors.toList());

        BigDecimal totalPending = pendingInvoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy danh sách hóa đơn quá hạn
        List<Invoice> overdueInvoices = invoiceRepository.findByCustomerId(customerId).stream()
                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PENDING && i.getDueDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());

        BigDecimal totalOverdue = overdueInvoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top hóa đơn quá hạn
        List<InvoiceDto> topOverdueInvoices = overdueInvoices.stream()
                .sorted((i1, i2) -> i2.getAmount().compareTo(i1.getAmount()))
                .limit(5)
                .map(invoiceService::convertToDto)
                .collect(Collectors.toList());

        ReportDto reportDto = new ReportDto();
        reportDto.setReportType("CUSTOMER");
        reportDto.setPeriod("Customer: " + customer.getFullName() + " (" + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
        reportDto.setTotalIncome(totalIncome);
        reportDto.setTotalPending(totalPending);
        reportDto.setTotalOverdue(totalOverdue);
        reportDto.setTotalInvoices(invoiceRepository.findByCustomerId(customerId).size());
        reportDto.setPaidInvoices((int) invoiceRepository.findByCustomerId(customerId).stream().filter(i -> i.getStatus() == Invoice.InvoiceStatus.PAID).count());
        reportDto.setPendingInvoices(pendingInvoices.size());
        reportDto.setOverdueInvoices(overdueInvoices.size());
        reportDto.setTopOverdueInvoices(topOverdueInvoices);

        return reportDto;
    }

    public ReportDto generateRoomReport(Long roomId, LocalDate startDate, LocalDate endDate) {
        // Kiểm tra phòng
        RoomDto room = roomClient.getRoomById(roomId).getBody();
        if (room == null) {
            throw new ResourceNotFoundException("Không tìm thấy phòng với ID: " + roomId);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);

        List<Payment> payments = paymentRepository.findByRoomId(roomId).stream()
                .filter(p -> p.getPaymentDate().isAfter(startDateTime) && p.getPaymentDate().isBefore(endDateTime))
                .collect(Collectors.toList());

        BigDecimal totalIncome = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy danh sách hóa đơn chưa thanh toán
        List<Invoice> pendingInvoices = invoiceRepository.findByRoomId(roomId).stream()
                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PENDING)
                .collect(Collectors.toList());

        BigDecimal totalPending = pendingInvoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy danh sách hóa đơn quá hạn
        List<Invoice> overdueInvoices = invoiceRepository.findByRoomId(roomId).stream()
                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PENDING && i.getDueDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());

        BigDecimal totalOverdue = overdueInvoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top hóa đơn quá hạn
        List<InvoiceDto> topOverdueInvoices = overdueInvoices.stream()
                .sorted((i1, i2) -> i2.getAmount().compareTo(i1.getAmount()))
                .limit(5)
                .map(invoiceService::convertToDto)
                .collect(Collectors.toList());

        ReportDto reportDto = new ReportDto();
        reportDto.setReportType("ROOM");
        reportDto.setPeriod("Room: " + room.getRoomNumber() + " (" + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
        reportDto.setTotalIncome(totalIncome);
        reportDto.setTotalPending(totalPending);
        reportDto.setTotalOverdue(totalOverdue);
        reportDto.setTotalInvoices(invoiceRepository.findByRoomId(roomId).size());
        reportDto.setPaidInvoices((int) invoiceRepository.findByRoomId(roomId).stream().filter(i -> i.getStatus() == Invoice.InvoiceStatus.PAID).count());
        reportDto.setPendingInvoices(pendingInvoices.size());
        reportDto.setOverdueInvoices(overdueInvoices.size());
        reportDto.setTopOverdueInvoices(topOverdueInvoices);

        return reportDto;
    }
}