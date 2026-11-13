package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;

/**
 * Abstract base class representing a person interacting with the reservation system.
 */
public abstract class Person {

    protected final String id;
    protected String fullName;
    protected String email;
    protected String phoneNumber;

    protected Person(String id, String fullName, String email, String phoneNumber) {
        this.id = ValidationException.requireNonBlank(id, "Person ID");
        this.fullName = ValidationException.requireNonBlank(fullName, "Full name");
        this.email = ValidationException.requireNonBlank(email, "Email");
        this.phoneNumber = ValidationException.requireNonBlank(phoneNumber, "Phone number");
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

    public abstract String getRole();
}
