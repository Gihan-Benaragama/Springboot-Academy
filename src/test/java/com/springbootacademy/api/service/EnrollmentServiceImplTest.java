package com.springbootacademy.api.service;

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
import com.springbootacademy.api.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private EnrollmentMapper enrollmentMapper;

    private EnrollmentServiceImpl service;

    private Student student;
    private Course course;
    private EnrollmentRequestDTO request;

    @BeforeEach
    void setUp() {
        service = new EnrollmentServiceImpl(enrollmentRepository, studentRepository, courseRepository, enrollmentMapper);

        student = new Student();
        student.setId(1L);
        student.setFullName("Test Student");
        student.setEmail("test@example.com");
        student.setStatus(StudentStatus.ACTIVE);

        course = new Course();
        course.setId(2L);
        course.setCode("SB-101");
        course.setStatus(CourseStatus.ACTIVE);
        course.setTotalSeats(5);
        course.setAvailableSeats(5);

        request = new EnrollmentRequestDTO();
        request.setStudentId(1L);
        request.setCourseId(2L);
        request.setPaid(true);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(anyLong(), anyLong(), any())).thenReturn(false);
        when(enrollmentMapper.toEntity(any(EnrollmentRequestDTO.class))).thenAnswer(inv -> new Enrollment());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(enrollmentMapper.toResponse(any(Enrollment.class))).thenReturn(new EnrollmentResponseDTO());
    }

    @Test
    void enroll_success_decreasesAvailableSeatsByOne() {
        service.enrollStudent(request);

        assertEquals(4, course.getAvailableSeats());
        verify(courseRepository).save(course);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enroll_noSeats_throwsBusinessException_andSavesNothing() {
        course.setAvailableSeats(0);

        assertThrows(BusinessException.class, () -> service.enrollStudent(request));

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
        verify(courseRepository, never()).save(any(Course.class));
        assertEquals(0, course.getAvailableSeats());
    }

    @Test
    void enroll_inactiveStudent_throwsBusinessException() {
        student.setStatus(StudentStatus.INACTIVE);
        assertThrows(BusinessException.class, () -> service.enrollStudent(request));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enroll_inactiveCourse_throwsBusinessException() {
        course.setStatus(CourseStatus.INACTIVE);
        assertThrows(BusinessException.class, () -> service.enrollStudent(request));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enroll_alreadyEnrolled_throwsBusinessException() {
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(1L, 2L, EnrollmentStatus.ENROLLED))
                .thenReturn(true);
        assertThrows(BusinessException.class, () -> service.enrollStudent(request));
        assertEquals(5, course.getAvailableSeats());
    }

    @Test
    void enroll_unknownStudent_throwsNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.enrollStudent(request));
    }

    @Test
    void cancel_enrolled_increasesAvailableSeatsByOne() {
        course.setAvailableSeats(3);
        Enrollment enrollment = new Enrollment();
        enrollment.setId(10L);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        when(enrollmentRepository.findById(10L)).thenReturn(Optional.of(enrollment));

        service.cancelEnrollment(10L);

        assertEquals(EnrollmentStatus.CANCELLED, enrollment.getStatus());
        assertEquals(4, course.getAvailableSeats());
    }

    @Test
    void cancel_alreadyCancelled_throwsBusinessException() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(10L);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        when(enrollmentRepository.findById(10L)).thenReturn(Optional.of(enrollment));

        assertThrows(BusinessException.class, () -> service.cancelEnrollment(10L));
        verify(courseRepository, never()).save(any(Course.class));
        assertEquals(5, course.getAvailableSeats());
    }

    @Test
    void cancel_unknownEnrollment_throwsNotFound() {
        when(enrollmentRepository.findById(eq(99L))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.cancelEnrollment(99L));
    }
}
