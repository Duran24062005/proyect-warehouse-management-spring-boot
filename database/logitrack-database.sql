DROP DATABASE IF EXISTS logiTrack;

CREATE DATABASE IF NOT EXISTS logiTrack
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE logiTrack;

-- =========================
-- Schema
-- =========================

CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    hash_password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'USER', 'EMPLOYEE') NOT NULL DEFAULT 'USER',
    user_status ENUM('PENDING', 'ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'PENDING',
    enable BOOLEAN NOT NULL DEFAULT TRUE,
    warehouse_id BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_At DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_app_user_status_enabled CHECK (
        (user_status = 'ACTIVE' AND enable = TRUE)
        OR (user_status IN ('PENDING', 'BLOCKED') AND enable = FALSE)
    ),
    KEY ix_app_user_warehouse (warehouse_id),
    UNIQUE KEY uq_app_user_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    ubication VARCHAR(255) NOT NULL,
    capacity DECIMAL(12, 3) NULL,
    manager_user_id BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_At DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY ix_warehouse_manager (manager_user_id),
    CONSTRAINT chk_warehouse_capacity CHECK (capacity IS NULL OR capacity >= 0),
    CONSTRAINT fk_warehouse_manager
        FOREIGN KEY (manager_user_id) REFERENCES app_user(id)
        ON DELETE SET NULL
        ON UPDATE RESTRICT
) ENGINE=InnoDB;

ALTER TABLE app_user
    ADD CONSTRAINT fk_app_user_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
        ON DELETE SET NULL
        ON UPDATE RESTRICT;

CREATE TABLE IF NOT EXISTS product (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(120) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    warehouse_id BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_At DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY ix_product_warehouse (warehouse_id),
    CONSTRAINT chk_product_price CHECK (price >= 0),
    CONSTRAINT fk_product_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
        ON DELETE SET NULL
        ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS entity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(120) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_At DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_entity_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_change (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    operation_type ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    actor_user_id BIGINT UNSIGNED NULL,
    affected_entity_id BIGINT UNSIGNED NULL,
    old_values JSON NULL,
    new_values JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_At DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY ix_audit_date (created_at),
    KEY ix_audit_actor_date (actor_user_id, created_at),
    KEY ix_audit_entity_date (affected_entity_id, created_at),
    CONSTRAINT fk_audit_actor
        FOREIGN KEY (actor_user_id) REFERENCES app_user(id)
        ON DELETE SET NULL
        ON UPDATE RESTRICT,
    CONSTRAINT fk_audit_entity
        FOREIGN KEY (affected_entity_id) REFERENCES entity(id)
        ON DELETE SET NULL
        ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movement (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    movement_type ENUM('ENTRY', 'EXIT', 'TRANSFER') NOT NULL,
    registered_by_user_id BIGINT UNSIGNED NOT NULL,
    performed_by_employee_id BIGINT UNSIGNED NOT NULL,
    origin_warehouse_id BIGINT UNSIGNED NULL,
    destination_warehouse_id BIGINT UNSIGNED NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_At DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY ix_movement_date (created_at),
    KEY ix_movement_registered_by_date (registered_by_user_id, created_at),
    KEY ix_movement_performed_by_date (performed_by_employee_id, created_at),
    KEY ix_movement_product_date (product_id, created_at),
    CONSTRAINT chk_movement_warehouses CHECK (
        (movement_type = 'ENTRY' AND origin_warehouse_id IS NULL AND destination_warehouse_id IS NOT NULL)
        OR (movement_type = 'EXIT' AND origin_warehouse_id IS NOT NULL AND destination_warehouse_id IS NULL)
        OR (
            movement_type = 'TRANSFER'
            AND origin_warehouse_id IS NOT NULL
            AND destination_warehouse_id IS NOT NULL
            AND origin_warehouse_id <> destination_warehouse_id
        )
    ),
    CONSTRAINT fk_movement_registered_by
        FOREIGN KEY (registered_by_user_id) REFERENCES app_user(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_movement_performed_by
        FOREIGN KEY (performed_by_employee_id) REFERENCES app_user(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_mov_origin
        FOREIGN KEY (origin_warehouse_id) REFERENCES warehouse(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_mov_dest
        FOREIGN KEY (destination_warehouse_id) REFERENCES warehouse(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_movement_product
        FOREIGN KEY (product_id) REFERENCES product(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- =========================
-- Seed data
-- =========================

INSERT INTO app_user (
    id,
    email,
    hash_password,
    first_name,
    last_name,
    phone_number,
    role,
    user_status,
    enable,
    warehouse_id
) VALUES
    (1, 'admin@logitrack.com', '$2a$10$adminhashlogitrack', 'Alexi', 'Duran', '3000000001', 'ADMIN', 'ACTIVE', TRUE, NULL),
    (2, 'mlopez@logitrack.com', '$2a$10$mlopezhashlogitrack', 'Maria', 'Lopez', '3000000002', 'USER', 'ACTIVE', TRUE, NULL),
    (3, 'jgarcia@logitrack.com', '$2a$10$jgarciahashlogitrack', 'Juan', 'Garcia', '3000000003', 'USER', 'PENDING', FALSE, NULL),
    (4, 'cperez@logitrack.com', '$2a$10$cperezhashlogitrack', 'Camila', 'Perez', '3000000004', 'USER', 'BLOCKED', FALSE, NULL);

INSERT INTO warehouse (
    id,
    name,
    ubication,
    capacity,
    manager_user_id,
    created_at
) VALUES
    (1, 'Bodega Central Bogota', 'Bogota, DC', 5000.000, 2, '2026-03-01 09:00:00'),
    (2, 'Bodega Norte Medellin', 'Medellin, Antioquia', 3200.000, 3, '2026-03-01 09:10:00'),
    (3, 'Bodega Costa Barranquilla', 'Barranquilla, Atlantico', 2800.000, 4, '2026-03-01 09:20:00');

INSERT INTO app_user (
    id,
    email,
    hash_password,
    first_name,
    last_name,
    phone_number,
    role,
    user_status,
    enable,
    warehouse_id
) VALUES
    (5, 'lrojas@logitrack.com', '$2a$10$lrojashashlogitrack', 'Luis', 'Rojas', '3000000005', 'EMPLOYEE', 'ACTIVE', TRUE, 1);

INSERT INTO product (
    id,
    name,
    category,
    price,
    warehouse_id,
    created_at
) VALUES
    (1, 'Laptop Dell Latitude 5440', 'Computo', 4200.00, 2, '2026-03-01 09:30:00'),
    (2, 'Monitor Samsung 24', 'Perifericos', 780.00, 1, '2026-03-01 09:35:00'),
    (3, 'Impresora Termica Zebra ZD220', 'Impresion', 1350.00, 2, '2026-03-01 09:40:00'),
    (4, 'Router TP-Link AX55', 'Redes', 560.00, 2, '2026-03-01 09:45:00'),
    (5, 'Scanner Honeywell 1470g', 'Logistica', 920.00, 1, '2026-03-01 09:50:00');

INSERT INTO entity (
    id,
    name,
    description,
    created_at
) VALUES
    (1, 'app_user', 'Catalog for application users', '2026-03-01 10:00:00'),
    (2, 'warehouse', 'Catalog for warehouses', '2026-03-01 10:01:00'),
    (3, 'product', 'Catalog for products', '2026-03-01 10:02:00'),
    (4, 'movement', 'Catalog for product movements', '2026-03-01 10:03:00');

INSERT INTO audit_change (
    id,
    operation_type,
    actor_user_id,
    affected_entity_id,
    old_values,
    new_values,
    created_at
) VALUES
    (
        1,
        'INSERT',
        1,
        1,
        NULL,
        JSON_OBJECT('table', 'app_user', 'inserted_rows', 5),
        '2026-03-01 10:10:00'
    ),
    (
        2,
        'INSERT',
        1,
        2,
        NULL,
        JSON_OBJECT('table', 'warehouse', 'inserted_rows', 3),
        '2026-03-01 10:12:00'
    ),
    (
        3,
        'INSERT',
        1,
        3,
        NULL,
        JSON_OBJECT('table', 'product', 'inserted_rows', 5),
        '2026-03-01 10:15:00'
    ),
    (
        4,
        'INSERT',
        2,
        4,
        NULL,
        JSON_OBJECT('movement_id', 1, 'product_id', 1, 'type', 'ENTRY'),
        '2026-03-02 10:05:00'
    ),
    (
        5,
        'UPDATE',
        2,
        3,
        JSON_OBJECT('warehouse_id', 1),
        JSON_OBJECT('warehouse_id', 2),
        '2026-03-04 14:10:00'
    );

INSERT INTO movement (
    id,
    movement_type,
    registered_by_user_id,
    performed_by_employee_id,
    origin_warehouse_id,
    destination_warehouse_id,
    product_id,
    created_at
) VALUES
    (1, 'ENTRY', 2, 5, NULL, 1, 1, '2026-03-02 10:00:00'),
    (2, 'ENTRY', 2, 5, NULL, 1, 2, '2026-03-02 10:30:00'),
    (3, 'ENTRY', 3, 5, NULL, 2, 3, '2026-03-03 09:00:00'),
    (4, 'TRANSFER', 2, 5, 1, 2, 1, '2026-03-04 14:00:00'),
    (5, 'EXIT', 3, 5, 2, NULL, 3, '2026-03-05 16:00:00'),
    (6, 'ENTRY', 4, 5, NULL, 1, 5, '2026-03-06 11:00:00'),
    (7, 'TRANSFER', 4, 5, 1, 3, 5, '2026-03-07 12:00:00'),
    (8, 'EXIT', 2, 5, 1, NULL, 2, '2026-03-08 15:30:00'),
    (9, 'ENTRY', 3, 5, NULL, 1, 4, '2026-03-09 09:45:00');

-- Sample queries are available in:
-- database/logitrack-queries.sql
