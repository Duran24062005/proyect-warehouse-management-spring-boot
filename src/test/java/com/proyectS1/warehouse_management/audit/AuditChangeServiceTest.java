package com.proyectS1.warehouse_management.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectS1.warehouse_management.dtos.response.AuditChangeResponseDTO;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.AuditChange;
import com.proyectS1.warehouse_management.model.TrackedEntity;
import com.proyectS1.warehouse_management.model.enums.OperationType;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.repositories.AuditChangeRepository;
import com.proyectS1.warehouse_management.services.impl.AuditChangeServiceImpl;
import com.proyectS1.warehouse_management.services.support.WarehouseAccessService;

class AuditChangeServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void managerOnlySeesAuditChangesWithinManagedWarehouses() {
        AppUser manager = buildUser(7L, UserRole.USER, "manager@logitrack.com", "Manager", "Uno");
        AuditChangeRepository auditRepository = buildRepository(List.of(
            buildAuditChange(
                1L,
                OperationType.UPDATE,
                "product",
                "Catalog for products",
                buildUser(100L, UserRole.ADMIN, "admin@logitrack.com", "Admin", "Root"),
                null,
                "{\"id\":9,\"warehouseId\":5,\"name\":\"Laptop\"}",
                LocalDateTime.now().minusMinutes(3)
            ),
            buildAuditChange(
                2L,
                OperationType.DELETE,
                "product",
                "Catalog for products",
                buildUser(101L, UserRole.ADMIN, "admin2@logitrack.com", "Admin", "Dos"),
                "{\"id\":10,\"warehouseId\":8,\"name\":\"Mouse\"}",
                null,
                LocalDateTime.now().minusMinutes(1)
            )
        ));

        AuditChangeServiceImpl service = new AuditChangeServiceImpl(
            auditRepository,
            new StubWarehouseAccessService(manager, false, List.of(5L)),
            objectMapper
        );

        List<AuditChangeResponseDTO> results = service.findVisibleAuditChanges(null, null, null, null);

        assertEquals(1, results.size());
        assertEquals(1L, results.getFirst().id());
        assertEquals("product", results.getFirst().entityName());
    }

    @Test
    void employeeCannotAccessAuditHistory() {
        AppUser employee = buildUser(9L, UserRole.EMPLOYEE, "employee@logitrack.com", "Ana", "Field");
        AuditChangeServiceImpl service = new AuditChangeServiceImpl(
            buildRepository(List.of()),
            new StubWarehouseAccessService(employee, false, List.of()),
            objectMapper
        );

        assertThrows(
            ResponseStatusException.class,
            () -> service.findVisibleAuditChanges(null, null, null, null)
        );
    }

    private AppUser buildUser(Long id, UserRole role, String email, String firstName, String lastName) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setRole(role);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

    private AuditChange buildAuditChange(
        Long id,
        OperationType operationType,
        String entityName,
        String entityDescription,
        AppUser actor,
        String oldValues,
        String newValues,
        LocalDateTime createdAt
    ) {
        TrackedEntity trackedEntity = new TrackedEntity();
        trackedEntity.setId(id + 100);
        trackedEntity.setName(entityName);
        trackedEntity.setDescription(entityDescription);

        AuditChange auditChange = new AuditChange();
        auditChange.setId(id);
        auditChange.setCreatedAt(createdAt);
        auditChange.setUpdatedAt(createdAt);
        auditChange.setOperationType(operationType);
        auditChange.setActorUser(actor);
        auditChange.setAffectedEntity(trackedEntity);
        auditChange.setOldValues(oldValues);
        auditChange.setNewValues(newValues);
        return auditChange;
    }

    private AuditChangeRepository buildRepository(List<AuditChange> auditChanges) {
        return (AuditChangeRepository) Proxy.newProxyInstance(
            AuditChangeRepository.class.getClassLoader(),
            new Class<?>[] {AuditChangeRepository.class},
            (_proxy, method, _args) -> switch (method.getName()) {
                case "findAllByOrderByCreatedAtDesc", "findAll" -> auditChanges;
                case "toString" -> "StubAuditChangeRepository";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> false;
                default -> throw new UnsupportedOperationException("Unexpected method: " + method.getName());
            }
        );
    }

    private static final class StubWarehouseAccessService extends WarehouseAccessService {
        private final AppUser currentUser;
        private final boolean admin;
        private final List<Long> managedWarehouseIds;

        private StubWarehouseAccessService(AppUser currentUser, boolean admin, List<Long> managedWarehouseIds) {
            super(null, null);
            this.currentUser = currentUser;
            this.admin = admin;
            this.managedWarehouseIds = managedWarehouseIds;
        }

        @Override
        public AppUser getCurrentUser() {
            return currentUser;
        }

        @Override
        public boolean isAdmin(AppUser user) {
            return admin;
        }

        @Override
        public java.util.Set<Long> getManagedWarehouseIds(AppUser user) {
            return new java.util.LinkedHashSet<>(managedWarehouseIds);
        }
    }
}
