package com.springbootacademy.api.mapper;

import com.springbootacademy.api.dto.EnrollmentRequestDTO;
import com.springbootacademy.api.dto.EnrollmentResponseDTO;
import com.springbootacademy.api.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    /** Student, course, date and status are set by the service after validation. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "enrolledDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Enrollment toEntity(EnrollmentRequestDTO dto);

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentName")
    @Mapping(source = "student.email", target = "studentEmail")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.name", target = "courseName")
    @Mapping(source = "course.code", target = "courseCode")
    EnrollmentResponseDTO toResponse(Enrollment enrollment);
}
