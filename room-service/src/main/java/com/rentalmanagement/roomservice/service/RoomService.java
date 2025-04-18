// service/RoomService.java
package com.rentalmanagement.roomservice.service;

import com.rentalmanagement.roomservice.dto.RoomDto;
import com.rentalmanagement.roomservice.exception.ResourceNotFoundException;
import com.rentalmanagement.roomservice.model.Room;
import com.rentalmanagement.roomservice.model.RoomType;
import com.rentalmanagement.roomservice.repository.RoomRepository;
import com.rentalmanagement.roomservice.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
        return convertToDto(room);
    }

    public RoomDto getRoomByNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với số phòng: " + roomNumber));
        return convertToDto(room);
    }

    public List<RoomDto> getRoomsByStatus(Room.RoomStatus status) {
        return roomRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public RoomDto createRoom(RoomDto roomDto) {
        Room room = convertToEntity(roomDto);
        Room savedRoom = roomRepository.save(room);
        return convertToDto(savedRoom);
    }

    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        Room room = convertToEntity(roomDto);
        room.setId(id);
        Room updatedRoom = roomRepository.save(room);

        return convertToDto(updatedRoom);
    }

    public RoomDto updateRoomStatus(Long id, Room.RoomStatus status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        room.setStatus(status);
        Room updatedRoom = roomRepository.save(room);

        return convertToDto(updatedRoom);
    }

    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        roomRepository.delete(room);
    }

    private RoomDto convertToDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRoomTypeId(room.getRoomType().getId());
        dto.setRoomTypeName(room.getRoomType().getName());
        dto.setMonthlyPrice(room.getMonthlyPrice());
        dto.setStatus(room.getStatus());
        dto.setDescription(room.getDescription());
        return dto;
    }

    private Room convertToEntity(RoomDto roomDto) {
        Room room = new Room();
        room.setId(roomDto.getId());
        room.setRoomNumber(roomDto.getRoomNumber());

        RoomType roomType = roomTypeRepository.findById(roomDto.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + roomDto.getRoomTypeId()));

        room.setRoomType(roomType);
        room.setMonthlyPrice(roomDto.getMonthlyPrice());

        if (roomDto.getStatus() != null) {
            room.setStatus(roomDto.getStatus());
        }

        room.setDescription(roomDto.getDescription());
        return room;
    }
}