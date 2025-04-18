// service/BookingService.java
package com.rentalmanagement.bookingservice.service;

import com.rentalmanagement.bookingservice.client.CustomerClient;
import com.rentalmanagement.bookingservice.client.RoomClient;
import com.rentalmanagement.bookingservice.dto.BookingDto;
import com.rentalmanagement.bookingservice.dto.CustomerDto;
import com.rentalmanagement.bookingservice.dto.RoomDto;
import com.rentalmanagement.bookingservice.exception.ResourceNotFoundException;
import com.rentalmanagement.bookingservice.model.Booking;
import com.rentalmanagement.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final CustomerClient customerClient;
    private final RoomClient roomClient;

    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng với ID: " + id));
        return convertToDto(booking);
    }

    public List<BookingDto> getBookingsByCustomerId(Long customerId) {
        return bookingRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> searchBookings(Long customerId, Long bookingId) {
        return bookingRepository.findByCustomerIdOrBookingId(customerId, bookingId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BookingDto createBooking(BookingDto bookingDto) {
        // Kiểm tra khách hàng
        CustomerDto customer = customerClient.getCustomerById(bookingDto.getCustomerId()).getBody();
        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + bookingDto.getCustomerId());
        }

        // Kiểm tra phòng
        RoomDto room = roomClient.getRoomById(bookingDto.getRoomId()).getBody();
        if (room == null) {
            throw new ResourceNotFoundException("Không tìm thấy phòng với ID: " + bookingDto.getRoomId());
        }

        // Cập nhật trạng thái phòng thành BOOKED
        roomClient.updateRoomStatus(bookingDto.getRoomId(), "BOOKED");

        Booking booking = convertToEntity(bookingDto);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        return convertToDto(savedBooking);
    }

    public BookingDto updateBooking(Long id, BookingDto bookingDto) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng với ID: " + id));

        // Nếu thay đổi phòng, cập nhật trạng thái phòng cũ và mới
        if (!existingBooking.getRoomId().equals(bookingDto.getRoomId())) {
            // Cập nhật phòng cũ thành AVAILABLE
            roomClient.updateRoomStatus(existingBooking.getRoomId(), "AVAILABLE");

            // Cập nhật phòng mới thành BOOKED
            roomClient.updateRoomStatus(bookingDto.getRoomId(), "BOOKED");
        }

        Booking booking = convertToEntity(bookingDto);
        booking.setId(id);
        booking.setBookingDate(existingBooking.getBookingDate());
        booking.setCreatedAt(existingBooking.getCreatedAt());

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDto(updatedBooking);
    }

    public BookingDto updateBookingStatus(Long id, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng với ID: " + id));

        // Nếu hủy đặt phòng, cập nhật trạng thái phòng thành AVAILABLE
        if (status == Booking.BookingStatus.CANCELLED) {
            roomClient.updateRoomStatus(booking.getRoomId(), "AVAILABLE");
        }

        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);

        return convertToDto(updatedBooking);
    }

    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng với ID: " + id));

        // Cập nhật trạng thái phòng thành AVAILABLE
        roomClient.updateRoomStatus(booking.getRoomId(), "AVAILABLE");

        bookingRepository.delete(booking);
    }

    private BookingDto convertToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setCustomerId(booking.getCustomerId());
        dto.setRoomId(booking.getRoomId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setStatus(booking.getStatus());
        dto.setNotes(booking.getNotes());

        // Lấy thông tin chi tiết khách hàng và phòng
        try {
            CustomerDto customer = customerClient.getCustomerById(booking.getCustomerId()).getBody();
            if (customer != null) {
                dto.setCustomerName(customer.getFullName());
            }

            RoomDto room = roomClient.getRoomById(booking.getRoomId()).getBody();
            if (room != null) {
                dto.setRoomNumber(room.getRoomNumber());
            }
        } catch (Exception e) {
            // Xử lý khi dịch vụ khác không khả dụng
        }

        return dto;
    }

    private Booking convertToEntity(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setCustomerId(bookingDto.getCustomerId());
        booking.setRoomId(bookingDto.getRoomId());
        booking.setCheckInDate(bookingDto.getCheckInDate());

        if (bookingDto.getStatus() != null) {
            booking.setStatus(bookingDto.getStatus());
        }

        booking.setNotes(bookingDto.getNotes());
        return booking;
    }
}