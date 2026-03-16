USE logiTrack;

ALTER TABLE app_user
    MODIFY COLUMN role ENUM('ADMIN', 'USER', 'EMPLOYEE') NOT NULL DEFAULT 'USER';

ALTER TABLE app_user
    ADD COLUMN warehouse_id BIGINT UNSIGNED NULL AFTER enable;

ALTER TABLE app_user
    ADD KEY ix_app_user_warehouse (warehouse_id);

ALTER TABLE app_user
    ADD CONSTRAINT fk_app_user_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
        ON DELETE SET NULL
        ON UPDATE RESTRICT;
