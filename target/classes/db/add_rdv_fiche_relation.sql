-- Lier chaque rendez-vous a une fiche medicale (patient + medecin).
SET @db = DATABASE();

SET @c := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
           WHERE BINARY TABLE_SCHEMA = BINARY @db
             AND BINARY TABLE_NAME = BINARY 'rdv'
             AND BINARY COLUMN_NAME = BINARY 'fiche_id');
SET @sql := IF(@c = 0, 'ALTER TABLE `rdv` ADD COLUMN `fiche_id` INT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
            WHERE BINARY CONSTRAINT_SCHEMA = BINARY @db
              AND BINARY CONSTRAINT_NAME = BINARY 'fk_rdv_fiche_id');
SET @sql_fk := IF(@fk = 0,
                  'ALTER TABLE `rdv` ADD CONSTRAINT `fk_rdv_fiche_id` FOREIGN KEY (`fiche_id`) REFERENCES `fiche`(`id`) ON UPDATE CASCADE ON DELETE SET NULL',
                  'SELECT 1');
PREPARE stmt_fk FROM @sql_fk; EXECUTE stmt_fk; DEALLOCATE PREPARE stmt_fk;

SET @idx := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
             WHERE BINARY TABLE_SCHEMA = BINARY @db
               AND BINARY TABLE_NAME = BINARY 'rdv'
               AND BINARY INDEX_NAME = BINARY 'idx_rdv_fiche_id');
SET @sql_idx := IF(@idx = 0, 'ALTER TABLE `rdv` ADD INDEX `idx_rdv_fiche_id` (`fiche_id`)', 'SELECT 1');
PREPARE stmt_idx FROM @sql_idx; EXECUTE stmt_idx; DEALLOCATE PREPARE stmt_idx;
