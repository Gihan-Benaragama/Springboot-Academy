package com.springbootacademy.api.repository.spec;

import com.springbootacademy.api.entity.Student;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class StudentSpecifications {

    private StudentSpecifications() {
    }

    /** Case-insensitive "contains" search on full name OR email. Blank search matches everything. */
    public static Specification<Student> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern));
        };
    }
}
