package com.springbootacademy.api.repository;

import com.springbootacademy.api.entity.Enrollment;
import com.springbootacademy.api.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {

    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, EnrollmentStatus status);

    /** Fetches student and course together with the enrollment (avoids N+1 selects when listing). */
    @Override
    @EntityGraph(attributePaths = {"student", "course"})
    Page<Enrollment> findAll(Specification<Enrollment> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "course"})
    Optional<Enrollment> findWithDetailsById(Long id);
}
