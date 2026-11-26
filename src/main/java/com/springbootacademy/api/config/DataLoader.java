package com.springbootacademy.api.config;

import com.springbootacademy.api.entity.Course;
import com.springbootacademy.api.entity.CourseStatus;
import com.springbootacademy.api.entity.Student;
import com.springbootacademy.api.entity.StudentStatus;
import com.springbootacademy.api.repository.CourseRepository;
import com.springbootacademy.api.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Inserts a few demo rows on startup when the tables are empty. Disable with app.seed-data=false. */
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public DataLoader(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        if (courseRepository.count() == 0) {
            courseRepository.save(course("Spring Boot", "SB-101", "Build REST APIs with Spring Boot", "25000.00", 30));
            courseRepository.save(course("Angular", "AN-101", "Modern front-end with Angular", "20000.00", 25));
            courseRepository.save(course("DevOps", "DO-101", "CI/CD, Docker and Kubernetes basics", "30000.00", 20));
            log.info("Seeded sample courses");
        }
        if (studentRepository.count() == 0) {
            studentRepository.save(student("Nimal Perera", "nimal@example.com", "0771234567"));
            studentRepository.save(student("Kasun Silva", "kasun@example.com", "0712345678"));
            studentRepository.save(student("Amali Fernando", "amali@example.com", "0753456789"));
            log.info("Seeded sample students");
        }
    }

    private Course course(String name, String code, String description, String fee, int seats) {
        Course c = new Course();
        c.setName(name);
        c.setCode(code);
        c.setDescription(description);
        c.setFee(new BigDecimal(fee));
        c.setTotalSeats(seats);
        c.setAvailableSeats(seats);
        c.setStatus(CourseStatus.ACTIVE);
        return c;
    }

    private Student student(String fullName, String email, String contact) {
        Student s = new Student();
        s.setFullName(fullName);
        s.setEmail(email);
        s.setContactNumber(contact);
        s.setStatus(StudentStatus.ACTIVE);
        return s;
    }
}
