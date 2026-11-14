package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void supportsDefaultAndExplicitPasswordAuthenticationAndChangePassword() {
        Admin defaultAdmin = new Admin("ADM-01", "Tran Van A", "admin@airline.vn", "0901234567", "STF-01", "Operations");
        assertTrue(defaultAdmin.authenticate("123456"));
        assertFalse(defaultAdmin.authenticate("bad"));

        Admin customAdmin = new Admin("ADM-02", "Le Thi B", "ops@airline.vn", "0907654321", "STF-02", "Control", "opsPass");
        assertTrue(customAdmin.authenticate("opsPass"));
        assertFalse(customAdmin.authenticate("123456"));

        customAdmin.changePassword("opsPass", "opsNew99");
        assertTrue(customAdmin.authenticate("opsNew99"));
        assertThrows(ValidationException.class, () -> customAdmin.changePassword("opsPass", "x"));
        assertThrows(ValidationException.class, () -> new Admin("ADM-03", "C", "c@airline.vn", "090", "STF-03", "Ops", null));
    }
}
