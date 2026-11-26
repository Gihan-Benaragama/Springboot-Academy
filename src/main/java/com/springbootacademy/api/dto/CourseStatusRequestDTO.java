package com.springbootacademy.api.dto;

import com.springbootacademy.api.entity.CourseStatus;
import jakarta.validation.constraints.NotNull;

public class CourseStatusRequestDTO {

    @NotNull(message = "Status is required (ACTIVE or INACTIVE)")
    private CourseStatus status;

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
}
