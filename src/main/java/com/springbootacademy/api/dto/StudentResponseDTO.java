package com.springbootacademy.api.dto;

import com.springbootacademy.api.entity.StudentStatus;

public class StudentResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String contactNumber;
    private StudentStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }
}
