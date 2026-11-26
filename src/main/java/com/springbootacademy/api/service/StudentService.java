package com.springbootacademy.api.service;

import com.springbootacademy.api.dto.StudentRequestDTO;
import com.springbootacademy.api.dto.StudentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    StudentResponseDTO createStudent(StudentRequestDTO request);

    StudentResponseDTO getStudentById(Long id);

    Page<StudentResponseDTO> getStudents(String search, Pageable pageable);

    StudentResponseDTO updateStudent(Long id, StudentRequestDTO request);

    /** Soft delete: sets the student status to INACTIVE. */
    void deleteStudent(Long id);
}
