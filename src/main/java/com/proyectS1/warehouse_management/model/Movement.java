package com.proyectS1.warehouse_management.model;

import com.proyectS1.warehouse_management.model.enums.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movement extends BaseTimeEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 10)
    private MovementType movementType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_user_id", nullable = false)
    private AppUser employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_warehouse_id")
    private Warehouse originWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_warehouse_id")
    private Warehouse destinationWarehouse;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;
}
