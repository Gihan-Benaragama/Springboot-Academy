package com.springbootacademy.api.service.impl;

import com.springbootacademy.api.dto.CourseRequestDTO;
import com.springbootacademy.api.dto.CourseResponseDTO;
import com.springbootacademy.api.entity.Course;
import com.springbootacademy.api.entity.CourseStatus;
import com.springbootacademy.api.exception.BusinessException;
import com.springbootacademy.api.exception.DuplicateResourceException;
import com.springbootacademy.api.exception.ResourceNotFoundException;
import com.springbootacademy.api.mapper.CourseMapper;
import com.springbootacademy.api.mapper.PageMapper;
import com.springbootacademy.api.repository.CourseRepository;
import com.springbootacademy.api.repository.spec.CourseSpecifications;
import com.springbootacademy.api.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final PageMapper pageMapper;

    public CourseServiceImpl(CourseRepository courseRepository, CourseMapper courseMapper, PageMapper pageMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.pageMapper = pageMapper;
    }

    @Override
    @Transactional
    public CourseResponseDTO createCourse(CourseRequestDTO request) {
        normalize(request);
        if (courseRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateResourceException("A course with code '" + request.getCode() + "' already exists");
        }
        Course course = courseMapper.toEntity(request);
        course.setAvailableSeats(request.getTotalSeats());   // nobody is enrolled yet
        if (course.getStatus() == null) {
            course.setStatus(CourseStatus.ACTIVE);
        }
        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponseDTO getCourseById(Long id) {
        return courseMapper.toResponse(findCourse(id));
    }

    @Override
    public Page<CourseResponseDTO> getCourses(CourseStatus status, BigDecimal minFee, BigDecimal maxFee, Pageable pageable) {
        if (minFee != null && maxFee != null && minFee.compareTo(maxFee) > 0) {
            throw new BusinessException("minFee must not be greater than maxFee");
        }
        Specification<Course> spec = Specification.allOf(
                CourseSpecifications.hasStatus(status),
                CourseSpecifications.feeAtLeast(minFee),
                CourseSpecifications.feeAtMost(maxFee));
        Page<Course> page = courseRepository.findAll(spec, pageable);
        // ModelMapper: dynamic Page<Entity> -> Page<DTO> conversion
        return pageMapper.toPage(page, CourseResponseDTO.class);
    }

    @Override
    @Transactional
    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO request) {
        // lock the row: seat arithmetic below must not race with enrollments
        Course course = courseRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        normalize(request);
        if (courseRepository.existsByCodeIgnoreCaseAndIdNot(request.getCode(), id)) {
            throw new DuplicateResourceException("A course with code '" + request.getCode() + "' already exists");
        }

        int enrolledSeats = course.getTotalSeats() - course.getAvailableSeats();
        if (request.getTotalSeats() < enrolledSeats) {
            throw new BusinessException("Total seats (" + request.getTotalSeats()
                    + ") cannot be less than the number of seats already taken (" + enrolledSeats + ")");
        }

        courseMapper.updateEntity(request, course);
        course.setAvailableSeats(request.getTotalSeats() - enrolledSeats);
        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponseDTO updateCourseStatus(Long id, CourseStatus status) {
        Course course = findCourse(id);
        course.setStatus(status);
        return courseMapper.toResponse(courseRepository.save(course));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    private void normalize(CourseRequestDTO request) {
        request.setName(request.getName().trim());
        request.setCode(request.getCode().trim().toUpperCase(Locale.ROOT));
        if (request.getDescription() != null) {
            request.setDescription(request.getDescription().trim());
        }
    }
}
