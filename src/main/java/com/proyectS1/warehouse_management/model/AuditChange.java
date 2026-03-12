package com.proyectS1.warehouse_management.model;

import com.proyectS1.warehouse_management.model.enums.OperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_change")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditChange extends BaseTimeEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 10)
    private OperationType operationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private AppUser actorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_entity_id")
    private TrackedEntity affectedEntity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "json")
    private String oldValues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "json")
    private String newValues;
}
