package com.springbootacademy.api.service;

import com.springbootacademy.api.dto.CourseRequestDTO;
import com.springbootacademy.api.dto.CourseResponseDTO;
import com.springbootacademy.api.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CourseService {

    CourseResponseDTO createCourse(CourseRequestDTO request);

    CourseResponseDTO getCourseById(Long id);

    Page<CourseResponseDTO> getCourses(CourseStatus status, BigDecimal minFee, BigDecimal maxFee, Pageable pageable);

    CourseResponseDTO updateCourse(Long id, CourseRequestDTO request);

    CourseResponseDTO updateCourseStatus(Long id, CourseStatus status);
}
