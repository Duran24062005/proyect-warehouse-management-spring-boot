package com.proyectS1.warehouse_management.mapper;

import org.springframework.stereotype.Component;

import com.proyectS1.warehouse_management.dtos.request.WarehouseRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.WarehouseResponseDTO;
import com.proyectS1.warehouse_management.model.Warehouse;

@Component
public class WarehouseMapper {

    public WarehouseResponseDTO entityToDTO(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }

        String managerName = warehouse.getManager() == null
            ? null
            : warehouse.getManager().getFirstName() + " " + warehouse.getManager().getLastName();

        return new WarehouseResponseDTO(
            warehouse.getId(),
            warehouse.getName(),
            warehouse.getUbication(),
            warehouse.getCapacity(),
            warehouse.getManager() != null ? warehouse.getManager().getId() : null,
            managerName
        );
    }

    public Warehouse dtoToEntity(WarehouseRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setName(dto.name());
        warehouse.setUbication(dto.ubication());
        warehouse.setCapacity(dto.capacity());
        return warehouse;
    }

    public void updateEntityFromDTO(Warehouse warehouse, WarehouseRequestDTO dto) {
        if (warehouse == null || dto == null) {
            return;
        }

        warehouse.setName(dto.name());
        warehouse.setUbication(dto.ubication());
        warehouse.setCapacity(dto.capacity());
    }
}
