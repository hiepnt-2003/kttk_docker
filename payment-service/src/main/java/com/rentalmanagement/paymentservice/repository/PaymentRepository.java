// repository/PaymentRepository.java
package com.rentalmanagement.paymentservice.repository;

import com.rentalmanagement.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceId(Long invoiceId);

    List<Payment> findByCustomerId(Long customerId);

    List<Payment> findByRoomId(Long roomId);

    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);

    List<Payment> findByMonthYear(String monthYear);

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN ?1 AND ?2")
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.monthYear = ?1")
    BigDecimal getTotalAmountByMonth(String monthYear);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentDate BETWEEN ?1 AND ?2")
    BigDecimal getTotalAmountBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
}