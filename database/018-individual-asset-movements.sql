USE logiTrack;

ALTER TABLE movement
    DROP CHECK chk_movement_quantity;

ALTER TABLE movement
    DROP COLUMN quantity;
