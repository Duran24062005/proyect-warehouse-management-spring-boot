USE logiTrack;

ALTER TABLE app_user
    ADD COLUMN profile_photo_filename VARCHAR(255) NULL AFTER user_status;
