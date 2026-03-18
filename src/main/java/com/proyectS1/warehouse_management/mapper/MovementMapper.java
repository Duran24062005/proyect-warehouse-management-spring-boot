package com.proyectS1.warehouse_management.mapper;

import org.springframework.stereotype.Component;

import com.proyectS1.warehouse_management.dtos.request.MovementRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.MovementResponseDTO;
import com.proyectS1.warehouse_management.model.Movement;

@Component
public class MovementMapper {

    public MovementResponseDTO entityToDTO(Movement movement) {
        if (movement == null) {
            return null;
        }

        String registeredByName = movement.getRegisteredByUser() == null
            ? null
            : movement.getRegisteredByUser().getFirstName() + " " + movement.getRegisteredByUser().getLastName();

        String performedByEmployeeName = movement.getPerformedByEmployee() == null
            ? null
            : movement.getPerformedByEmployee().getFirstName() + " " + movement.getPerformedByEmployee().getLastName();

        return new MovementResponseDTO(
            movement.getId(),
            movement.getMovementType(),
            movement.getRegisteredByUser() != null ? movement.getRegisteredByUser().getId() : null,
            registeredByName,
            movement.getPerformedByEmployee() != null ? movement.getPerformedByEmployee().getId() : null,
            performedByEmployeeName,
            movement.getOriginWarehouse() != null ? movement.getOriginWarehouse().getId() : null,
            movement.getOriginWarehouse() != null ? movement.getOriginWarehouse().getName() : null,
            movement.getDestinationWarehouse() != null ? movement.getDestinationWarehouse().getId() : null,
            movement.getDestinationWarehouse() != null ? movement.getDestinationWarehouse().getName() : null,
            movement.getProduct() != null ? movement.getProduct().getId() : null,
            movement.getProduct() != null ? movement.getProduct().getName() : null,
            movement.getCreatedAt()
        );
    }

    public Movement dtoToEntity(MovementRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Movement movement = new Movement();
        movement.setMovementType(dto.movementType());
        return movement;
    }

    public void updateEntityFromDTO(Movement movement, MovementRequestDTO dto) {
        if (movement == null || dto == null) {
            return;
        }

        movement.setMovementType(dto.movementType());
    }
}
