package com.springbootacademy.api.mapper;

import com.springbootacademy.api.dto.CourseRequestDTO;
import com.springbootacademy.api.dto.CourseResponseDTO;
import com.springbootacademy.api.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {

    /** availableSeats is derived by the service (= totalSeats on creation). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    Course toEntity(CourseRequestDTO dto);

    CourseResponseDTO toResponse(Course course);

    /** availableSeats is recalculated by the service so enrolled seats are never lost. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    void updateEntity(CourseRequestDTO dto, @MappingTarget Course course);
}
