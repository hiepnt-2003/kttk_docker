// repository/RoomTypeRepository.java
package com.rentalmanagement.roomservice.repository;

import com.rentalmanagement.roomservice.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
}
