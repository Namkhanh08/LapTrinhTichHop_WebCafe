CREATE DATABASE IF NOT EXISTS revo_inventory;
USE revo_inventory;

CREATE TABLE IF NOT EXISTS inventory_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255),
    quantity_available INT NOT NULL DEFAULT 0,
    quantity_reserved INT NOT NULL DEFAULT 0,
    warehouse_location VARCHAR(100),
    reorder_level INT DEFAULT 20,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stock_movements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    movement_type ENUM('in', 'out', 'reserve', 'release', 'adjustment') NOT NULL,
    reference_id VARCHAR(100),
    reference_type VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO inventory_items (product_id, product_name, quantity_available, quantity_reserved, warehouse_location, reorder_level) VALUES
('1', 'REVO Morning', 99250, 0, 'BC2-WH-A1', 50),
('2', 'REVO Everyday', 99749, 0, 'BC2-WH-A2', 50),
('3', 'REVO Origin', 99496, 0, 'BC2-WH-A3', 50),
('4', 'REVO Đậm Đà', 99000, 0, 'BC2-WH-A4', 50),
('5', 'REVO Robusta', 99249, 0, 'BC2-WH-A5', 50);

INSERT INTO stock_movements (product_id, quantity, movement_type, reference_id, reference_type, notes) VALUES
('1', 99250, 'in', 'BC2-SEED-1', 'legacy_import', 'Initial stock imported from BC2.sql'),
('2', 99749, 'in', 'BC2-SEED-2', 'legacy_import', 'Initial stock imported from BC2.sql'),
('3', 99496, 'in', 'BC2-SEED-3', 'legacy_import', 'Initial stock imported from BC2.sql'),
('4', 99000, 'in', 'BC2-SEED-4', 'legacy_import', 'Initial stock imported from BC2.sql'),
('5', 99249, 'in', 'BC2-SEED-5', 'legacy_import', 'Initial stock imported from BC2.sql');
