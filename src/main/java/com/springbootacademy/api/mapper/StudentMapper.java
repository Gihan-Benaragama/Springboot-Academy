package com.springbootacademy.api.mapper;

import com.springbootacademy.api.dto.StudentRequestDTO;
import com.springbootacademy.api.dto.StudentResponseDTO;
import com.springbootacademy.api.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentMapper {

    @Mapping(target = "id", ignore = true)
    Student toEntity(StudentRequestDTO dto);

    StudentResponseDTO toResponse(Student student);

    /** In-place update; null source properties (e.g. omitted status) keep the existing value. */
    @Mapping(target = "id", ignore = true)
    void updateEntity(StudentRequestDTO dto, @MappingTarget Student student);
}
