package com.springbootacademy.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class EnrollmentRequestDTO {

    @NotNull(message = "Student id is required")
    @Positive(message = "Student id must be a positive number")
    private Long studentId;

    @NotNull(message = "Course id is required")
    @Positive(message = "Course id must be a positive number")
    private Long courseId;

    private boolean paid;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}
