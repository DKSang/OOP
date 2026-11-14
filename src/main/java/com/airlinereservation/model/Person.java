package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;

/**
 * Abstract base class representing a person interacting with the reservation system.
 */
public abstract class Person {

    public static final String DEFAULT_PASSWORD = "123456";

    protected final String id;
    protected String fullName;
    protected String email;
    protected String phoneNumber;
    private String password;

    protected Person(String id, String fullName, String email, String phoneNumber) {
        this(id, fullName, email, phoneNumber, DEFAULT_PASSWORD);
    }

    protected Person(String id, String fullName, String email, String phoneNumber, String password) {
        this.id = ValidationException.requireNonBlank(id, "Person ID");
        this.fullName = ValidationException.requireNonBlank(fullName, "Full name");
        this.email = ValidationException.requireNonBlank(email, "Email");
        this.phoneNumber = ValidationException.requireNonBlank(phoneNumber, "Phone number");
        this.password = requireNonBlankPassword(password, "Password");
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void updateContactInfo(String email, String phone) {
        String validEmail = ValidationException.requireNonBlank(email, "Email");
        String validPhone = ValidationException.requireNonBlank(phone, "Phone number");
        this.email = validEmail;
        this.phoneNumber = validPhone;
    }

    public boolean authenticate(String candidatePassword) {
        return candidatePassword != null && this.password.equals(candidatePassword);
    }

    public void changePassword(String currentPassword, String newPassword) {
        if (!authenticate(currentPassword)) {
            throw new ValidationException("Current password is incorrect");
        }
        this.password = requireNonBlankPassword(newPassword, "New password");
    }

    public abstract String getRole();

    private static String requireNonBlankPassword(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
        return value;
    }
}
