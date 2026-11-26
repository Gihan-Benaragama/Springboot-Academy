package com.springbootacademy.api.repository.spec;

import com.springbootacademy.api.entity.Enrollment;
import com.springbootacademy.api.entity.EnrollmentStatus;
import org.springframework.data.jpa.domain.Specification;

public final class EnrollmentSpecifications {

    private EnrollmentSpecifications() {
    }

    public static Specification<Enrollment> hasStudentId(Long studentId) {
        return (root, query, cb) -> studentId == null
                ? cb.conjunction()
                : cb.equal(root.get("student").get("id"), studentId);
    }

    public static Specification<Enrollment> hasCourseId(Long courseId) {
        return (root, query, cb) -> courseId == null
                ? cb.conjunction()
                : cb.equal(root.get("course").get("id"), courseId);
    }

    public static Specification<Enrollment> hasStatus(EnrollmentStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
}
