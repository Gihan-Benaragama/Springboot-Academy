package com.springbootacademy.api.controller;

import com.springbootacademy.api.dto.StandardResponse;
import com.springbootacademy.api.dto.StudentRequestDTO;
import com.springbootacademy.api.dto.StudentResponseDTO;
import com.springbootacademy.api.service.StudentService;
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
@RequestMapping("/api/v1/students")
@Tag(name = "Students", description = "Student management")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "Create a new student", description = "Email must be unique.")
    @PostMapping
    public ResponseEntity<StandardResponse<StudentResponseDTO>> createStudent(
            @Valid @RequestBody StudentRequestDTO request) {
        StudentResponseDTO created = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success("Student created successfully", created));
    }

    @Operation(summary = "Get a student by id")
    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<StudentResponseDTO>> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(StandardResponse.success("Student retrieved successfully",
                studentService.getStudentById(id)));
    }

    @Operation(summary = "List students",
            description = "Paginated list. Optional 'search' matches full name or email (case-insensitive). "
                    + "Example: /api/v1/students?page=0&size=10&sort=fullName,asc&search=john")
    @GetMapping
    public ResponseEntity<StandardResponse<Page<StudentResponseDTO>>> getStudents(
            @Parameter(description = "Search text for name or email") @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(StandardResponse.success("Students retrieved successfully",
                studentService.getStudents(search, pageable)));
    }

    @Operation(summary = "Update a student")
    @PutMapping("/{id}")
    public ResponseEntity<StandardResponse<StudentResponseDTO>> updateStudent(
            @PathVariable Long id, @Valid @RequestBody StudentRequestDTO request) {
        return ResponseEntity.ok(StandardResponse.success("Student updated successfully",
                studentService.updateStudent(id, request)));
    }

    @Operation(summary = "Soft delete a student", description = "Sets the student status to INACTIVE; the row is kept.")
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(StandardResponse.<Void>success("Student deactivated successfully", null));
    }
}
