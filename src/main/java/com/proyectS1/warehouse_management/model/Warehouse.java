package com.proyectS1.warehouse_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "warehouse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse extends BaseTimeEntity {

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String ubication;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 12, scale = 3)
    private BigDecimal capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_user_id")
    private AppUser manager;
}
