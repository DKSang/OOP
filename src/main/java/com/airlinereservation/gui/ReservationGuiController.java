package com.airlinereservation.gui;

import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Admin;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;
import com.airlinereservation.system.AirlineReservationSystem;
import com.airlinereservation.system.DemoDataSeeder;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Passive-View controller mediating between Swing views and {@link AirlineReservationSystem}.
 */
public class ReservationGuiController {

    private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount("Passenger - PAX-01 (Nguyen Van An)", "PAX-01", Person.DEFAULT_PASSWORD, "Passenger"),
            new DemoAccount("Passenger - PAX-02 (Tran Thi Binh)", "PAX-02", Person.DEFAULT_PASSWORD, "Passenger"),
            new DemoAccount("Admin - ADM-01 (Le Minh Cuong)", "ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD, "Admin")
    );

    private final AirlineReservationSystem system;
    private Person currentUser;

    public ReservationGuiController(AirlineReservationSystem system) {
        this.system = ValidationException.requireNonNull(system, "Reservation system");
    }

    public Person login(String identifier, String password) {
        Person authenticated = system.authenticate(identifier, password);
        this.currentUser = authenticated;
        return authenticated;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Person getCurrentUser() {
        return currentUser;
    }

    public boolean isPassengerLoggedIn() {
        return currentUser instanceof Passenger;
    }

    public boolean isAdminLoggedIn() {
        return currentUser instanceof Admin;
    }

    public Passenger registerPassenger(String id,
                                       String fullName,
                                       String email,
                                       String phoneNumber,
                                       String dateOfBirthText,
                                       String gender,
                                       String passportOrId,
                                       String password) {
        String trimmedDob = ValidationException.requireNonBlank(dateOfBirthText, "Date of birth");
        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(trimmedDob);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Date of birth must follow YYYY-MM-DD format", ex);
        }
        return registerPassenger(id, fullName, email, phoneNumber, dateOfBirth, gender, passportOrId, password);
    }

    public Passenger registerPassenger(String id,
                                       String fullName,
                                       String email,
                                       String phoneNumber,
                                       LocalDate dateOfBirth,
                                       String gender,
                                       String passportOrId,
                                       String password) {
        Passenger passenger = new Passenger(
                id, fullName, email, phoneNumber, dateOfBirth, gender, passportOrId, password
        );
        system.registerPassenger(passenger);
        return passenger;
    }

    public List<DemoAccount> getDemoAccounts() {
        return DEMO_ACCOUNTS;
    }

    public void resetDemoData() {
        DemoDataSeeder.reset(system);
        this.currentUser = null;
    }
}
