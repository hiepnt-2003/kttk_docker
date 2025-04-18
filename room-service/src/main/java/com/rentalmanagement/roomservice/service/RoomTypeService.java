// service/RoomTypeService.java
package com.rentalmanagement.roomservice.service;

import com.rentalmanagement.roomservice.dto.RoomTypeDto;
import com.rentalmanagement.roomservice.exception.ResourceNotFoundException;
import com.rentalmanagement.roomservice.model.RoomType;
import com.rentalmanagement.roomservice.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;

    public List<RoomTypeDto> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public RoomTypeDto getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + id));
        return convertToDto(roomType);
    }

    public RoomTypeDto createRoomType(RoomTypeDto roomTypeDto) {
        RoomType roomType = convertToEntity(roomTypeDto);
        RoomType savedRoomType = roomTypeRepository.save(roomType);
        return convertToDto(savedRoomType);
    }

    public RoomTypeDto updateRoomType(Long id, RoomTypeDto roomTypeDto) {
        roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + id));

        RoomType roomType = convertToEntity(roomTypeDto);
        roomType.setId(id);
        RoomType updatedRoomType = roomTypeRepository.save(roomType);

        return convertToDto(updatedRoomType);
    }

    public void deleteRoomType(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng với ID: " + id));

        roomTypeRepository.delete(roomType);
    }

    private RoomTypeDto convertToDto(RoomType roomType) {
        RoomTypeDto dto = new RoomTypeDto();
        dto.setId(roomType.getId());
        dto.setName(roomType.getName());
        dto.setDescription(roomType.getDescription());
        return dto;
    }

    private RoomType convertToEntity(RoomTypeDto roomTypeDto) {
        RoomType roomType = new RoomType();
        roomType.setId(roomTypeDto.getId());
        roomType.setName(roomTypeDto.getName());
        roomType.setDescription(roomTypeDto.getDescription());
        return roomType;
    }
}