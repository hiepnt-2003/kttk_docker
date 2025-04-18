// dto/PaymentDto.java
package com.rentalmanagement.paymentservice.dto;

import com.rentalmanagement.paymentservice.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;

    private Long invoiceId;

    private String invoiceNumber;

    @NotNull(message = "ID khách hàng không được để trống")
    private Long customerId;

    private String customerName;

    @NotNull(message = "ID phòng không được để trống")
    private Long roomId;

    private String roomNumber;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    private LocalDateTime paymentDate;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private Payment.PaymentMethod paymentMethod;

    private String transactionId;

    @NotBlank(message = "Tháng/Năm không được để trống")
    private String monthYear;

    private String notes;
}