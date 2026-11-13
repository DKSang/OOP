package com.airlinereservation.model;

import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.ValidationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Passenger profile holding identity, travel document, and booking history.
 */
public class Passenger extends Person {

    private final LocalDate dateOfBirth;
    private final String gender;
    private String passportOrId;
    private final List<Booking> bookingHistory = new ArrayList<>();

    public Passenger(String id,
                     String fullName,
                     String email,
                     String phoneNumber,
                     LocalDate dateOfBirth,
                     String gender,
                     String passportOrId) {
        super(id, fullName, email, phoneNumber);
        this.dateOfBirth = ValidationException.requireNonNull(dateOfBirth, "Date of birth");
        this.gender = ValidationException.requireNonBlank(gender, "Gender");
        this.passportOrId = ValidationException.requireNonBlank(passportOrId, "Passport or ID");
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getPassportOrId() {
        return passportOrId;
    }

    public void updateProfile(String fullName, String passportOrId, String phone, String email) {
        String validName = ValidationException.requireNonBlank(fullName, "Full name");
        String validPassport = ValidationException.requireNonBlank(passportOrId, "Passport or ID");
        String validPhone = ValidationException.requireNonBlank(phone, "Phone number");
        String validEmail = ValidationException.requireNonBlank(email, "Email");
        this.fullName = validName;
        this.passportOrId = validPassport;
        updateContactInfo(validEmail, validPhone);
    }

    public void addBooking(Booking booking) {
        ValidationException.requireNonNull(booking, "Booking");
        if (booking.getPassenger() != this) {
            throw new ValidationException("Booking does not belong to passenger " + id);
        }
        if (bookingHistory.contains(booking)) {
            throw new DuplicateEntityException("Booking already recorded in passenger history: " + booking.getBookingId());
        }
        bookingHistory.add(booking);
    }

    public List<Booking> getBookingHistory() {
        return List.copyOf(bookingHistory);
    }

    @Override
    public String getRole() {
        return "Passenger";
    }
}
