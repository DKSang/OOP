package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminTest {

    @Test
    void createsAdminUpdatesContactInfoAndRejectsInvalidFields() {
        Person admin = new Admin(" ADM-01 ", " Tran Van A ", " admin@airline.vn ", " 0901234567 ", " STF-01 ", " Operations ");
        Admin typedAdmin = (Admin) admin;

        assertEquals("ADM-01", admin.getId());
        assertEquals("Tran Van A", admin.getFullName());
        assertEquals("admin@airline.vn", admin.getEmail());
        assertEquals("0901234567", admin.getPhoneNumber());
        assertEquals("STF-01", typedAdmin.getStaffCode());
        assertEquals("Operations", typedAdmin.getDepartment());
        assertEquals("Admin", admin.getRole());

        admin.updateContactInfo(" ops@airline.vn ", " 0987654321 ");
        assertEquals("ops@airline.vn", admin.getEmail());
        assertEquals("0987654321", admin.getPhoneNumber());

        assertThrows(ValidationException.class, () -> admin.updateContactInfo("  ", "0987654321"));
        assertThrows(ValidationException.class, () -> admin.updateContactInfo("ops@airline.vn", null));
        assertThrows(ValidationException.class, () -> new Admin(null, "A", "a@b.c", "090", "S1", "Ops"));
        assertThrows(ValidationException.class, () -> new Admin("ADM-02", " ", "a@b.c", "090", "S1", "Ops"));
        assertThrows(ValidationException.class, () -> new Admin("ADM-02", "A", "a@b.c", "090", " ", "Ops"));
        assertThrows(ValidationException.class, () -> new Admin("ADM-02", "A", "a@b.c", "090", "S1", null));
    }
}
