// service/CheckInService.java
package com.rentalmanagement.bookingservice.service;

import com.rentalmanagement.bookingservice.client.CustomerClient;
import com.rentalmanagement.bookingservice.client.RoomClient;
import com.rentalmanagement.bookingservice.dto.CheckInDto;
import com.rentalmanagement.bookingservice.dto.CustomerDto;
import com.rentalmanagement.bookingservice.dto.RoomDto;
import com.rentalmanagement.bookingservice.exception.ResourceNotFoundException;
import com.rentalmanagement.bookingservice.model.Booking;
import com.rentalmanagement.bookingservice.model.CheckIn;
import com.rentalmanagement.bookingservice.repository.BookingRepository;
import com.rentalmanagement.bookingservice.repository.CheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckInService {
    private final CheckInRepository checkInRepository;
    private final BookingRepository bookingRepository;
    private final CustomerClient customerClient;
    private final RoomClient roomClient;

    public List<CheckInDto> getAllCheckIns() {
        return checkInRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CheckInDto getCheckInById(Long id) {
        CheckIn checkIn = checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhận phòng với ID: " + id));
        return convertToDto(checkIn);
    }

    public List<CheckInDto> getCheckInsByCustomerId(Long customerId) {
        return checkInRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CheckInDto> getCheckInsByRoomId(Long roomId) {
        return checkInRepository.findByRoomId(roomId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CheckInDto getCheckInByBookingId(Long bookingId) {
        CheckIn checkIn = checkInRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhận phòng với ID đặt phòng: " + bookingId));
        return convertToDto(checkIn);
    }

    public List<CheckInDto> getCheckInsByStatus(CheckIn.CheckInStatus status) {
        return checkInRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CheckInDto createCheckIn(CheckInDto checkInDto) {
        // Kiểm tra khách hàng
        CustomerDto customer = customerClient.getCustomerById(checkInDto.getCustomerId()).getBody();
        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + checkInDto.getCustomerId());
        }

        // Kiểm tra phòng
        RoomDto room = roomClient.getRoomById(checkInDto.getRoomId()).getBody();
        if (room == null) {
            throw new ResourceNotFoundException("Không tìm thấy phòng với ID: " + checkInDto.getRoomId());
        }

        // Cập nhật trạng thái phòng thành OCCUPIED
        roomClient.updateRoomStatus(checkInDto.getRoomId(), "OCCUPIED");

        CheckIn checkIn = convertToEntity(checkInDto);
        checkIn.setCheckInDate(LocalDate.now());
        checkIn.setStatus(CheckIn.CheckInStatus.ACTIVE);

        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        return convertToDto(savedCheckIn);
    }

    public CheckInDto createCheckInFromBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng với ID: " + bookingId));

        if (booking.getStatus() == Booking.BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Đặt phòng này đã được nhận phòng");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Không thể nhận phòng cho đặt phòng đã hủy");
        }

        // Cập nhật trạng thái đặt phòng
        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);

        // Cập nhật trạng thái phòng thành OCCUPIED
        roomClient.updateRoomStatus(booking.getRoomId(), "OCCUPIED");

        // Tạo nhận phòng mới
        CheckIn checkIn = new CheckIn();
        checkIn.setBookingId(bookingId);
        checkIn.setCustomerId(booking.getCustomerId());
        checkIn.setRoomId(booking.getRoomId());
        checkIn.setCheckInDate(LocalDate.now());
        checkIn.setStatus(CheckIn.CheckInStatus.ACTIVE);

        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        return convertToDto(savedCheckIn);
    }

    public CheckInDto updateCheckIn(Long id, CheckInDto checkInDto) {
        CheckIn existingCheckIn = checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhận phòng với ID: " + id));

        // Nếu thay đổi phòng, cập nhật trạng thái phòng cũ và mới
        if (!existingCheckIn.getRoomId().equals(checkInDto.getRoomId())) {
            // Cập nhật phòng cũ thành AVAILABLE
            roomClient.updateRoomStatus(existingCheckIn.getRoomId(), "AVAILABLE");

            // Cập nhật phòng mới thành OCCUPIED
            roomClient.updateRoomStatus(checkInDto.getRoomId(), "OCCUPIED");
        }

        CheckIn checkIn = convertToEntity(checkInDto);
        checkIn.setId(id);
        checkIn.setCreatedAt(existingCheckIn.getCreatedAt());

        CheckIn updatedCheckIn = checkInRepository.save(checkIn);
        return convertToDto(updatedCheckIn);
    }

    public CheckInDto updateCheckInStatus(Long id, CheckIn.CheckInStatus status) {
        CheckIn checkIn = checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhận phòng với ID: " + id));

        // Nếu trả phòng, cập nhật trạng thái phòng thành AVAILABLE
        if (status == CheckIn.CheckInStatus.CHECKED_OUT) {
            roomClient.updateRoomStatus(checkIn.getRoomId(), "AVAILABLE");
        }

        checkIn.setStatus(status);
        CheckIn updatedCheckIn = checkInRepository.save(checkIn);

        return convertToDto(updatedCheckIn);
    }

    public void deleteCheckIn(Long id) {
        CheckIn checkIn = checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhận phòng với ID: " + id));

        // Cập nhật trạng thái phòng thành AVAILABLE
        roomClient.updateRoomStatus(checkIn.getRoomId(), "AVAILABLE");

        checkInRepository.delete(checkIn);
    }

    private CheckInDto convertToDto(CheckIn checkIn) {
        CheckInDto dto = new CheckInDto();
        dto.setId(checkIn.getId());
        dto.setBookingId(checkIn.getBookingId());
        dto.setCustomerId(checkIn.getCustomerId());
        dto.setRoomId(checkIn.getRoomId());
        dto.setCheckInDate(checkIn.getCheckInDate());
        dto.setExpectedCheckOutDate(checkIn.getExpectedCheckOutDate());
        dto.setStatus(checkIn.getStatus());

        // Lấy thông tin chi tiết khách hàng và phòng
        try {
            CustomerDto customer = customerClient.getCustomerById(checkIn.getCustomerId()).getBody();
            if (customer != null) {
                dto.setCustomerName(customer.getFullName());
            }

            RoomDto room = roomClient.getRoomById(checkIn.getRoomId()).getBody();
            if (room != null) {
                dto.setRoomNumber(room.getRoomNumber());
            }
        } catch (Exception e) {
            // Xử lý khi dịch vụ khác không khả dụng
        }

        return dto;
    }

    private CheckIn convertToEntity(CheckInDto checkInDto) {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(checkInDto.getId());
        checkIn.setBookingId(checkInDto.getBookingId());
        checkIn.setCustomerId(checkInDto.getCustomerId());
        checkIn.setRoomId(checkInDto.getRoomId());
        checkIn.setCheckInDate(checkInDto.getCheckInDate());
        checkIn.setExpectedCheckOutDate(checkInDto.getExpectedCheckOutDate());

        if (checkInDto.getStatus() != null) {
            checkIn.setStatus(checkInDto.getStatus());
        }

        return checkIn;
    }
}