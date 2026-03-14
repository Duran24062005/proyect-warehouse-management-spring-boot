package com.proyectS1.warehouse_management.services.support;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.repositories.WarehouseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarehouseAccessService {

    private final AppUserRepository appUserRepository;
    private final WarehouseRepository warehouseRepository;

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        return appUserRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authenticated user not found"));
    }

    public boolean isAdmin(AppUser user) {
        return user.getRole() == UserRole.ADMIN;
    }

    public void requireAdmin(AppUser user) {
        if (!isAdmin(user)) {
            throw new ResponseStatusException(FORBIDDEN, "Only admins can perform this operation");
        }
    }

    public Set<Long> getManagedWarehouseIds(AppUser user) {
        return warehouseRepository.findByManagerId(user.getId()).stream()
            .map(warehouse -> warehouse.getId())
            .collect(Collectors.toSet());
    }

    public void requireWarehouseAccess(AppUser user, Long warehouseId) {
        if (warehouseId == null || isAdmin(user)) {
            return;
        }

        if (!getManagedWarehouseIds(user).contains(warehouseId)) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied for warehouse " + warehouseId);
        }
    }

    public void requireWarehouseAccess(AppUser user, List<Long> warehouseIds) {
        if (isAdmin(user)) {
            return;
        }

        Set<Long> managedWarehouseIds = getManagedWarehouseIds(user);
        boolean hasAccess = warehouseIds.stream()
            .filter(warehouseId -> warehouseId != null)
            .allMatch(managedWarehouseIds::contains);

        if (!hasAccess) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied for one or more warehouses");
        }
    }
}
