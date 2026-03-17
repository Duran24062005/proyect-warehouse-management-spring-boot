package com.proyectS1.warehouse_management.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.Product;
import com.proyectS1.warehouse_management.model.Warehouse;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.model.enums.UserStatus;

class AuditServiceTest {

    private final AuditService auditService = new AuditService(
        null,
        null,
        null,
        new ObjectMapper()
    );

    @Test
    void captureAppUserSnapshotExcludesPasswordAndMarksPasswordChanges() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(99L);

        AppUser user = new AppUser();
        user.setId(10L);
        user.setEmail("user@logitrack.com");
        user.setFirstName("Maria");
        user.setLastName("Lopez");
        user.setPhoneNumber("3000000002");
        user.setRole(UserRole.USER);
        user.setUserStatus(UserStatus.ACTIVE);
        user.setEnabled(Boolean.TRUE);
        user.setWarehouse(warehouse);
        user.setProfilePhotoFilename("maria.png");
        user.setHashPassword("hash-v1");

        AuditSnapshot firstSnapshot = auditService.captureSnapshot(user, null);
        Map<String, Object> firstPayload = firstSnapshot.payload();

        assertEquals(10L, firstPayload.get("id"));
        assertEquals("user@logitrack.com", firstPayload.get("email"));
        assertEquals(99L, firstPayload.get("warehouseId"));
        assertEquals("maria.png", firstPayload.get("profilePhotoFilename"));
        assertFalse(firstPayload.containsKey("hashPassword"));
        assertFalse(firstPayload.containsKey("passwordChanged"));

        user.setHashPassword("hash-v2");
        AuditSnapshot updatedSnapshot = auditService.captureSnapshot(user, firstSnapshot);

        assertEquals("hash-v2", updatedSnapshot.passwordFingerprint());
        assertEquals(true, updatedSnapshot.payload().get("passwordChanged"));
        assertFalse(updatedSnapshot.payload().containsKey("hashPassword"));
    }

    @Test
    void captureProductSnapshotKeepsOnlySafeFields() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(4L);

        Product product = new Product();
        product.setId(7L);
        product.setName("Router TP-Link AX55");
        product.setCategory("Redes");
        product.setPrice(new BigDecimal("560.00"));
        product.setWarehouse(warehouse);

        AuditSnapshot snapshot = auditService.captureSnapshot(product, null);

        assertEquals(7L, snapshot.payload().get("id"));
        assertEquals("Router TP-Link AX55", snapshot.payload().get("name"));
        assertEquals("560.00", snapshot.payload().get("price"));
        assertEquals(4L, snapshot.payload().get("warehouseId"));
        assertNull(snapshot.passwordFingerprint());
    }
}
