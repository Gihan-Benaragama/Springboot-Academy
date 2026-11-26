package com.springbootacademy.api.service.impl;

import com.springbootacademy.api.dto.EnrollmentRequestDTO;
import com.springbootacademy.api.dto.EnrollmentResponseDTO;
import com.springbootacademy.api.entity.Course;
import com.springbootacademy.api.entity.CourseStatus;
import com.springbootacademy.api.entity.Enrollment;
import com.springbootacademy.api.entity.EnrollmentStatus;
import com.springbootacademy.api.entity.Student;
import com.springbootacademy.api.entity.StudentStatus;
import com.springbootacademy.api.exception.BusinessException;
import com.springbootacademy.api.exception.ResourceNotFoundException;
import com.springbootacademy.api.mapper.EnrollmentMapper;
import com.springbootacademy.api.repository.CourseRepository;
import com.springbootacademy.api.repository.EnrollmentRepository;
import com.springbootacademy.api.repository.StudentRepository;
import com.springbootacademy.api.repository.spec.EnrollmentSpecifications;
import com.springbootacademy.api.service.EnrollmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 StudentRepository studentRepository,
                                 CourseRepository courseRepository,
                                 EnrollmentMapper enrollmentMapper) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentMapper = enrollmentMapper;
    }

    /**
     * Enrolls a student in a course. Runs in ONE transaction: if any rule fails an exception
     * is thrown, the transaction rolls back and neither the enrollment nor the seat change is saved.
     */
    @Override
    @Transactional
    public EnrollmentResponseDTO enrollStudent(EnrollmentRequestDTO request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.getStudentId()));

        // pessimistic lock on the course row -> safe seat counting under concurrency
        Course course = courseRepository.findByIdForUpdate(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException("Student must be ACTIVE to enroll in a course");
        }
        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new BusinessException("Course must be ACTIVE to accept enrollments");
        }
        if (course.getAvailableSeats() == null || course.getAvailableSeats() <= 0) {
            throw new BusinessException("No seats available for course '" + course.getCode() + "'");
        }
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                student.getId(), course.getId(), EnrollmentStatus.ENROLLED)) {
            throw new BusinessException("Student is already enrolled in course '" + course.getCode() + "'");
        }

        Enrollment enrollment = enrollmentMapper.toEntity(request);   // maps "paid"
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledDate(LocalDate.now());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);

        course.setAvailableSeats(course.getAvailableSeats() - 1);
        courseRepository.save(course);

        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public Page<EnrollmentResponseDTO> getEnrollments(Long studentId, Long courseId,
                                                      EnrollmentStatus status, Pageable pageable) {
        Specification<Enrollment> spec = Specification.allOf(
                EnrollmentSpecifications.hasStudentId(studentId),
                EnrollmentSpecifications.hasCourseId(courseId),
                EnrollmentSpecifications.hasStatus(status));
        return enrollmentRepository.findAll(spec, pageable).map(enrollmentMapper::toResponse);
    }

    @Override
    public EnrollmentResponseDTO getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
        return enrollmentMapper.toResponse(enrollment);
    }

    /**
     * Cancels an ENROLLED enrollment and gives the seat back, atomically.
     */
    @Override
    @Transactional
    public EnrollmentResponseDTO cancelEnrollment(Long id) {
        // plain findById: the course stays a lazy reference, so the locked query below reads fresh seat data
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new BusinessException("Only ENROLLED enrollments can be cancelled (current status: "
                    + enrollment.getStatus() + ")");
        }

        Course course = courseRepository.findByIdForUpdate(enrollment.getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", enrollment.getCourse().getId()));

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        course.setAvailableSeats(Math.min(course.getAvailableSeats() + 1, course.getTotalSeats()));
        courseRepository.save(course);

        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }
}
