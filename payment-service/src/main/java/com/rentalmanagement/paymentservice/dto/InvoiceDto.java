// dto/InvoiceDto.java
package com.rentalmanagement.paymentservice.dto;

import com.rentalmanagement.paymentservice.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private Long id;

    private String invoiceNumber;

    @NotNull(message = "ID khách hàng không được để trống")
    private Long customerId;

    private String customerName;

    @NotNull(message = "ID phòng không được để trống")
    private Long roomId;

    private String roomNumber;

    @NotBlank(message = "Tháng/Năm không được để trống")
    private String monthYear;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Hạn thanh toán không được để trống")
    @Future(message = "Hạn thanh toán phải là ngày trong tương lai")
    private LocalDate dueDate;

    private Invoice.InvoiceStatus status;

    private String description;
}