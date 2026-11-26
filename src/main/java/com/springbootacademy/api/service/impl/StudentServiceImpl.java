package com.springbootacademy.api.service.impl;

import com.springbootacademy.api.dto.StudentRequestDTO;
import com.springbootacademy.api.dto.StudentResponseDTO;
import com.springbootacademy.api.entity.Student;
import com.springbootacademy.api.entity.StudentStatus;
import com.springbootacademy.api.exception.DuplicateResourceException;
import com.springbootacademy.api.exception.ResourceNotFoundException;
import com.springbootacademy.api.mapper.PageMapper;
import com.springbootacademy.api.mapper.StudentMapper;
import com.springbootacademy.api.repository.StudentRepository;
import com.springbootacademy.api.repository.spec.StudentSpecifications;
import com.springbootacademy.api.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final PageMapper pageMapper;

    public StudentServiceImpl(StudentRepository studentRepository, StudentMapper studentMapper, PageMapper pageMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
        this.pageMapper = pageMapper;
    }

    @Override
    @Transactional
    public StudentResponseDTO createStudent(StudentRequestDTO request) {
        normalize(request);
        if (studentRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A student with email '" + request.getEmail() + "' already exists");
        }
        Student student = studentMapper.toEntity(request);
        if (student.getStatus() == null) {
            student.setStatus(StudentStatus.ACTIVE);
        }
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Override
    public StudentResponseDTO getStudentById(Long id) {
        return studentMapper.toResponse(findStudent(id));
    }

    @Override
    public Page<StudentResponseDTO> getStudents(String search, Pageable pageable) {
        Page<Student> page = studentRepository.findAll(StudentSpecifications.search(search), pageable);
        // ModelMapper: dynamic Page<Entity> -> Page<DTO> conversion
        return pageMapper.toPage(page, StudentResponseDTO.class);
    }

    @Override
    @Transactional
    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {
        Student student = findStudent(id);
        normalize(request);
        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("A student with email '" + request.getEmail() + "' already exists");
        }
        studentMapper.updateEntity(request, student);
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = findStudent(id);
        student.setStatus(StudentStatus.INACTIVE);   // soft delete
        studentRepository.save(student);
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    private void normalize(StudentRequestDTO request) {
        request.setFullName(request.getFullName().trim());
        request.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        request.setContactNumber(request.getContactNumber().trim());
    }
}
