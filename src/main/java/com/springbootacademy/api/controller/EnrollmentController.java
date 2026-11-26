package com.springbootacademy.api.controller;

import com.springbootacademy.api.dto.EnrollmentRequestDTO;
import com.springbootacademy.api.dto.EnrollmentResponseDTO;
import com.springbootacademy.api.dto.StandardResponse;
import com.springbootacademy.api.entity.EnrollmentStatus;
import com.springbootacademy.api.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enrollments")
@Tag(name = "Enrollments", description = "Enrollment management")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @Operation(summary = "Enroll a student in a course",
            description = "Student and course must be ACTIVE, seats must be available and the student must not "
                    + "already be ENROLLED in the course. Decreases availableSeats by 1 (transactional).")
    @PostMapping
    public ResponseEntity<StandardResponse<EnrollmentResponseDTO>> enrollStudent(
            @Valid @RequestBody EnrollmentRequestDTO request) {
        EnrollmentResponseDTO created = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success("Student enrolled successfully", created));
    }

    @Operation(summary = "List enrollments",
            description = "Paginated list with optional filters by studentId, courseId and status. "
                    + "Example: /api/v1/enrollments?studentId=1&status=ENROLLED&page=0&size=10")
    @GetMapping
    public ResponseEntity<StandardResponse<Page<EnrollmentResponseDTO>>> getEnrollments(
            @Parameter(description = "Filter by student id") @RequestParam(required = false) Long studentId,
            @Parameter(description = "Filter by course id") @RequestParam(required = false) Long courseId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) EnrollmentStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(StandardResponse.success("Enrollments retrieved successfully",
                enrollmentService.getEnrollments(studentId, courseId, status, pageable)));
    }

    @Operation(summary = "Get enrollment details", description = "Includes basic student and course information.")
    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<EnrollmentResponseDTO>> getEnrollmentById(@PathVariable Long id) {
        return ResponseEntity.ok(StandardResponse.success("Enrollment retrieved successfully",
                enrollmentService.getEnrollmentById(id)));
    }

    @Operation(summary = "Cancel an enrollment",
            description = "Only possible while status = ENROLLED. Increases availableSeats by 1 (transactional).")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<StandardResponse<EnrollmentResponseDTO>> cancelEnrollment(@PathVariable Long id) {
        return ResponseEntity.ok(StandardResponse.success("Enrollment cancelled successfully",
                enrollmentService.cancelEnrollment(id)));
    }
}
