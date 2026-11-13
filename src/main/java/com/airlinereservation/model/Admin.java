package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;

/**
 * System administrator managing airline catalogs and flight operations.
 */
public class Admin extends Person {

    private final String staffCode;
    private final String department;

    public Admin(String id,
                 String fullName,
                 String email,
                 String phoneNumber,
                 String staffCode,
                 String department) {
        super(id, fullName, email, phoneNumber);
        this.staffCode = ValidationException.requireNonBlank(staffCode, "Staff code");
        this.department = ValidationException.requireNonBlank(department, "Department");
    }

    public String getStaffCode() {
        return staffCode;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}
