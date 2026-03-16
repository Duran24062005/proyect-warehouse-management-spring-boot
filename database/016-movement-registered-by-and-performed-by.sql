USE logiTrack;

ALTER TABLE movement
    DROP FOREIGN KEY fk_mov_emp;

ALTER TABLE movement
    DROP INDEX ix_movement_employee_date;

ALTER TABLE movement
    CHANGE COLUMN employee_user_id registered_by_user_id BIGINT UNSIGNED NOT NULL;

ALTER TABLE movement
    ADD COLUMN performed_by_employee_id BIGINT UNSIGNED NULL AFTER registered_by_user_id;

UPDATE movement
SET performed_by_employee_id = registered_by_user_id
WHERE performed_by_employee_id IS NULL;

ALTER TABLE movement
    MODIFY COLUMN performed_by_employee_id BIGINT UNSIGNED NOT NULL;

ALTER TABLE movement
    ADD KEY ix_movement_registered_by_date (registered_by_user_id, created_at),
    ADD KEY ix_movement_performed_by_date (performed_by_employee_id, created_at);

ALTER TABLE movement
    ADD CONSTRAINT fk_movement_registered_by
        FOREIGN KEY (registered_by_user_id) REFERENCES app_user(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    ADD CONSTRAINT fk_movement_performed_by
        FOREIGN KEY (performed_by_employee_id) REFERENCES app_user(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT;
