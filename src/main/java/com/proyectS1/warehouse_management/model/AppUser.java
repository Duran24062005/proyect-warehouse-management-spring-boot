package com.proyectS1.warehouse_management.model;

import com.proyectS1.warehouse_management.audit.AuditEntityListener;
import com.proyectS1.warehouse_management.audit.AuditableEntity;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.model.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_user")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser extends BaseTimeEntity implements AuditableEntity {

    @Email
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "hash_password", nullable = false, length = 255)
    private String hashPassword;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "phone_number", nullable = false, length = 100)
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.USER;

    @NotNull
    @Column(name = "enable", nullable = false)
    private Boolean enabled = Boolean.TRUE;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus = UserStatus.PENDING;

    @Column(name = "profile_photo_filename", length = 255)
    private String profilePhotoFilename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Override
    public String auditEntityName() {
        return "app_user";
    }

    @Override
    public String auditEntityDescription() {
        return "Catalog for application users";
    }
}
