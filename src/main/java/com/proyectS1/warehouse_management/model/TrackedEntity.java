package com.proyectS1.warehouse_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackedEntity extends BaseTimeEntity {

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String description;
}
