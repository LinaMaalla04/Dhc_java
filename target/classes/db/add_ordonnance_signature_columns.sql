-- SignatureAPI metadata for signed ordonnances.
SET @db = DATABASE();

SET @c1 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE BINARY TABLE_SCHEMA = BINARY @db
              AND BINARY TABLE_NAME = BINARY 'ordonnance'
              AND BINARY COLUMN_NAME = BINARY 'signature_envelope_id');
SET @sql1 := IF(@c1 = 0, 'ALTER TABLE `ordonnance` ADD COLUMN `signature_envelope_id` VARCHAR(128) NULL', 'SELECT 1');
PREPARE stmt1 FROM @sql1; EXECUTE stmt1; DEALLOCATE PREPARE stmt1;

SET @c2 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE BINARY TABLE_SCHEMA = BINARY @db
              AND BINARY TABLE_NAME = BINARY 'ordonnance'
              AND BINARY COLUMN_NAME = BINARY 'signature_ceremony_url');
SET @sql2 := IF(@c2 = 0, 'ALTER TABLE `ordonnance` ADD COLUMN `signature_ceremony_url` TEXT NULL', 'SELECT 1');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

SET @c3 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE BINARY TABLE_SCHEMA = BINARY @db
              AND BINARY TABLE_NAME = BINARY 'ordonnance'
              AND BINARY COLUMN_NAME = BINARY 'signature_deliverable_url');
SET @sql3 := IF(@c3 = 0, 'ALTER TABLE `ordonnance` ADD COLUMN `signature_deliverable_url` TEXT NULL', 'SELECT 1');
PREPARE stmt3 FROM @sql3; EXECUTE stmt3; DEALLOCATE PREPARE stmt3;

SET @c4 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE BINARY TABLE_SCHEMA = BINARY @db
              AND BINARY TABLE_NAME = BINARY 'ordonnance'
              AND BINARY COLUMN_NAME = BINARY 'signature_status');
SET @sql4 := IF(@c4 = 0, 'ALTER TABLE `ordonnance` ADD COLUMN `signature_status` VARCHAR(32) NULL', 'SELECT 1');
PREPARE stmt4 FROM @sql4; EXECUTE stmt4; DEALLOCATE PREPARE stmt4;

SET @c5 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE BINARY TABLE_SCHEMA = BINARY @db
              AND BINARY TABLE_NAME = BINARY 'ordonnance'
              AND BINARY COLUMN_NAME = BINARY 'signature_email_sent_at');
SET @sql5 := IF(@c5 = 0, 'ALTER TABLE `ordonnance` ADD COLUMN `signature_email_sent_at` TIMESTAMP NULL', 'SELECT 1');
PREPARE stmt5 FROM @sql5; EXECUTE stmt5; DEALLOCATE PREPARE stmt5;
