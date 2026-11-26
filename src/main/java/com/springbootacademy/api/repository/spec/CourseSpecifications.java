package com.springbootacademy.api.repository.spec;

import com.springbootacademy.api.entity.Course;
import com.springbootacademy.api.entity.CourseStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class CourseSpecifications {

    private CourseSpecifications() {
    }

    public static Specification<Course> hasStatus(CourseStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Course> feeAtLeast(BigDecimal minFee) {
        return (root, query, cb) -> minFee == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.<BigDecimal>get("fee"), minFee);
    }

    public static Specification<Course> feeAtMost(BigDecimal maxFee) {
        return (root, query, cb) -> maxFee == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.<BigDecimal>get("fee"), maxFee);
    }
}
