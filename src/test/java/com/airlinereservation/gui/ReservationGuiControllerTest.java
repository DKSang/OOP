package com.airlinereservation.gui;

import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Admin;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;
import com.airlinereservation.system.AirlineReservationSystem;
import com.airlinereservation.system.DemoDataSeeder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationGuiControllerTest {

    @Test
    void managesPassengerAndAdminLoginLogoutSessions() {
        ReservationGuiController controller = new ReservationGuiController(DemoDataSeeder.createSeededSystem());

        assertNull(controller.getCurrentUser());
        assertFalse(controller.isPassengerLoggedIn());
        assertFalse(controller.isAdminLoggedIn());

        Person passenger = controller.login("PAX-01", Person.DEFAULT_PASSWORD);
        assertInstanceOf(Passenger.class, passenger);
        assertEquals("PAX-01", controller.getCurrentUser().getId());
        assertTrue(controller.isPassengerLoggedIn());
        assertFalse(controller.isAdminLoggedIn());

        controller.logout();
        assertNull(controller.getCurrentUser());
        assertFalse(controller.isPassengerLoggedIn());

        Person admin = controller.login("ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD);
        assertInstanceOf(Admin.class, admin);
        assertTrue(controller.isAdminLoggedIn());
        assertFalse(controller.isPassengerLoggedIn());

        assertThrows(ValidationException.class, () -> controller.login("ADM-01", "wrong-pass"));
        assertTrue(controller.isAdminLoggedIn());
    }

    @Test
    void providesQuickFillDemoAccountsRegistersNewPassengerAndResetsDemoData() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        List<DemoAccount> accounts = controller.getDemoAccounts();
        assertEquals(3, accounts.size());
        for (DemoAccount account : accounts) {
            Person loggedIn = controller.login(account.identifier(), account.password());
            assertEquals(account.role(), loggedIn.getRole());
            controller.logout();
        }

        Passenger registered = controller.registerPassenger(
                "PAX-03",
                "Pham Gia Huy",
                "huy.pham@airline.vn",
                "0903334444",
                LocalDate.of(2002, 7, 15),
                "Male",
                "CCCD-0003",
                "mySecret99"
        );
        assertEquals("PAX-03", registered.getId());
        Person loggedInNew = controller.login("huy.pham@airline.vn", "mySecret99");
        assertEquals("PAX-03", loggedInNew.getId());
        assertTrue(controller.isPassengerLoggedIn());

        assertThrows(DuplicateEntityException.class, () -> controller.registerPassenger(
                "PAX-03", "Dup", "dup@airline.vn", "0900",
                LocalDate.of(2000, 1, 1), "Male", "CCCD-9999", "pass123"
        ));
        assertThrows(ValidationException.class, () -> controller.registerPassenger(
                "PAX-04", "Bad Date", "bad@airline.vn", "0900",
                "15-07-2002", "Female", "CCCD-8888", "pass123"
        ));

        controller.resetDemoData();
        assertNull(controller.getCurrentUser());
        assertFalse(controller.isPassengerLoggedIn());
        assertEquals(2, system.getPassengers().size());
        assertThrows(ValidationException.class, () -> controller.login("PAX-03", "mySecret99"));
    }
}
