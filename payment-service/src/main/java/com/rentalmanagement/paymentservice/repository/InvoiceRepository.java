// repository/InvoiceRepository.java (tiếp)
package com.rentalmanagement.paymentservice.repository;

import com.rentalmanagement.paymentservice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findByRoomId(Long roomId);

    List<Invoice> findByStatus(Invoice.InvoiceStatus status);

    List<Invoice> findByMonthYear(String monthYear);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < ?1 AND i.status = 'PENDING'")
    List<Invoice> findOverdueInvoices(LocalDate currentDate);

    @Query("SELECT i FROM Invoice i WHERE i.customerId = ?1 AND i.roomId = ?2 AND i.monthYear = ?3 AND (i.status = 'PENDING' OR i.status = 'OVERDUE')")
    List<Invoice> findPendingInvoicesByCustomerAndRoomAndMonth(Long customerId, Long roomId, String monthYear);

    @Query("SELECT i FROM Invoice i WHERE (i.invoiceNumber LIKE %?1% OR CAST(i.id AS string) = ?1)")
    List<Invoice> searchInvoices(String searchTerm);
}