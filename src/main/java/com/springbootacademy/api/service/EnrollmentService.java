package com.springbootacademy.api.service;

import com.springbootacademy.api.dto.EnrollmentRequestDTO;
import com.springbootacademy.api.dto.EnrollmentResponseDTO;
import com.springbootacademy.api.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {

    EnrollmentResponseDTO enrollStudent(EnrollmentRequestDTO request);

    Page<EnrollmentResponseDTO> getEnrollments(Long studentId, Long courseId, EnrollmentStatus status, Pageable pageable);

    EnrollmentResponseDTO getEnrollmentById(Long id);

    EnrollmentResponseDTO cancelEnrollment(Long id);
}
