package com.springbootacademy.api.dto;

import com.springbootacademy.api.entity.CourseStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CourseRequestDTO {

    @NotBlank(message = "Course name is required")
    @Size(max = 100, message = "Course name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code must not exceed 20 characters")
    private String code;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Fee is required")
    @DecimalMin(value = "0.0", message = "Fee cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Fee must have at most 10 integer digits and 2 decimal places")
    private BigDecimal fee;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be greater than 0")
    private Integer totalSeats;

    /** Optional. Defaults to ACTIVE on create; unchanged on update when omitted. */
    private CourseStatus status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
}
