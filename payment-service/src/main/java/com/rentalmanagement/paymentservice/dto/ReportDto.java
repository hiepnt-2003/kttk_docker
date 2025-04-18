// dto/ReportDto.java
package com.rentalmanagement.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private String reportType;
    private String period;
    private BigDecimal totalIncome;
    private BigDecimal totalPending;
    private BigDecimal totalOverdue;
    private int totalInvoices;
    private int paidInvoices;
    private int pendingInvoices;
    private int overdueInvoices;
    private Map<String, BigDecimal> incomeByPeriod;
    private Map<String, BigDecimal> incomeByPaymentMethod;
    private List<InvoiceDto> topOverdueInvoices;
}