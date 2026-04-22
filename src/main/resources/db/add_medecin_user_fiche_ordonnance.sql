-- Lien fiche / ordonnance → médecin créateur (nullable pour données historiques).
SET @db = DATABASE();

SET @c := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fiche' AND COLUMN_NAME = 'medecin_user_id');
SET @sql := IF(@c = 0, 'ALTER TABLE `fiche` ADD COLUMN `medecin_user_id` INT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c2 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'ordonnance' AND COLUMN_NAME = 'medecin_user_id');
SET @sql2 := IF(@c2 = 0, 'ALTER TABLE `ordonnance` ADD COLUMN `medecin_user_id` INT NULL', 'SELECT 1');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;
