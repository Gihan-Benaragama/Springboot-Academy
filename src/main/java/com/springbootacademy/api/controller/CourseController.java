package com.springbootacademy.api.controller;

import com.springbootacademy.api.dto.CourseRequestDTO;
import com.springbootacademy.api.dto.CourseResponseDTO;
import com.springbootacademy.api.dto.CourseStatusRequestDTO;
import com.springbootacademy.api.dto.StandardResponse;
import com.springbootacademy.api.entity.CourseStatus;
import com.springbootacademy.api.service.CourseService;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "Course management")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(summary = "Create a new course", description = "Course code must be unique (e.g. SB-101).")
    @PostMapping
    public ResponseEntity<StandardResponse<CourseResponseDTO>> createCourse(
            @Valid @RequestBody CourseRequestDTO request) {
        CourseResponseDTO created = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success("Course created successfully", created));
    }

    @Operation(summary = "Get a course by id")
    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<CourseResponseDTO>> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(StandardResponse.success("Course retrieved successfully",
                courseService.getCourseById(id)));
    }

    @Operation(summary = "List courses",
            description = "Paginated + sortable list with optional filters. "
                    + "Example: /api/v1/courses?page=0&size=10&sort=fee,desc&status=ACTIVE&minFee=100&maxFee=500")
    @GetMapping
    public ResponseEntity<StandardResponse<Page<CourseResponseDTO>>> getCourses(
            @Parameter(description = "Filter by status") @RequestParam(required = false) CourseStatus status,
            @Parameter(description = "Minimum fee (inclusive)") @RequestParam(required = false) BigDecimal minFee,
            @Parameter(description = "Maximum fee (inclusive)") @RequestParam(required = false) BigDecimal maxFee,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(StandardResponse.success("Courses retrieved successfully",
                courseService.getCourses(status, minFee, maxFee, pageable)));
    }

    @Operation(summary = "Update a course")
    @PutMapping("/{id}")
    public ResponseEntity<StandardResponse<CourseResponseDTO>> updateCourse(
            @PathVariable Long id, @Valid @RequestBody CourseRequestDTO request) {
        return ResponseEntity.ok(StandardResponse.success("Course updated successfully",
                courseService.updateCourse(id, request)));
    }

    @Operation(summary = "Activate / deactivate a course", description = "Body: {\"status\": \"ACTIVE\"} or {\"status\": \"INACTIVE\"}")
    @PatchMapping("/{id}/status")
    public ResponseEntity<StandardResponse<CourseResponseDTO>> updateCourseStatus(
            @PathVariable Long id, @Valid @RequestBody CourseStatusRequestDTO request) {
        return ResponseEntity.ok(StandardResponse.success("Course status updated successfully",
                courseService.updateCourseStatus(id, request.getStatus())));
    }
}
