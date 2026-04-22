-- Migration idempotente : ré-exécutable sans erreur "Duplicate column" / table déjà là

-- 1) Colonne stock sur medicament (uniquement si elle n'existe pas encore)
SET @sql_stock = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE medicament ADD COLUMN stock INT NOT NULL DEFAULT 0',
        'SELECT ''medicament.stock existe déjà'' AS info'
    )
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medicament'
      AND COLUMN_NAME = 'stock'
);
PREPARE stmt_stock FROM @sql_stock;
EXECUTE stmt_stock;
DEALLOCATE PREPARE stmt_stock;

-- 2) Table de liaison ordonnance ↔ médicament
CREATE TABLE IF NOT EXISTS ordonnance_medicament (
    ordonnance_id INT NOT NULL,
    medicament_id INT NOT NULL,
    PRIMARY KEY (ordonnance_id, medicament_id)
);
