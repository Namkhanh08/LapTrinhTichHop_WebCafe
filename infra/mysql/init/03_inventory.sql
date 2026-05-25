SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS revo_inventory
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE revo_inventory;

CREATE TABLE IF NOT EXISTS warehouses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS raw_material_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    contact_name VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255),
    quantity_available INT NOT NULL DEFAULT 0,
    quantity_reserved INT NOT NULL DEFAULT 0,
    warehouse_id INT NULL,
    warehouse_location VARCHAR(100),
    reorder_level INT DEFAULT 20,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product (product_id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE SET NULL,
    CHECK (quantity_available >= 0),
    CHECK (quantity_reserved >= 0),
    CHECK (reorder_level >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stock_movements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    movement_type ENUM('in', 'out', 'reserve', 'release', 'adjustment') NOT NULL,
    reference_id VARCHAR(100),
    reference_type VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stock_movements_product_id (product_id),
    INDEX idx_stock_movements_reference (reference_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS raw_material_lots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    raw_material_type_id INT NULL,
    bean_type VARCHAR(100) NOT NULL,
    supplier_id INT NULL,
    supplier VARCHAR(255) NOT NULL,
    quantity_kg DECIMAL(10, 2) NOT NULL,
    quantity_remaining_kg DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    received_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    origin_region VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (raw_material_type_id) REFERENCES raw_material_types(id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    INDEX idx_raw_material_lots_type (raw_material_type_id),
    INDEX idx_raw_material_lots_supplier (supplier_id),
    INDEX idx_raw_material_lots_expiration (expiration_date),
    CHECK (quantity_kg > 0),
    CHECK (quantity_remaining_kg >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO warehouses (id, code, name, location) VALUES
(1, 'BC2-WH-A1', 'Kho thanh pham A1', 'BC2'),
(2, 'BC2-WH-A2', 'Kho thanh pham A2', 'BC2'),
(3, 'BC2-WH-A3', 'Kho thanh pham A3', 'BC2'),
(4, 'BC2-WH-A4', 'Kho thanh pham A4', 'BC2'),
(5, 'BC2-WH-A5', 'Kho thanh pham A5', 'BC2')
ON DUPLICATE KEY UPDATE name = VALUES(name), location = VALUES(location);

INSERT INTO raw_material_types (id, name, description) VALUES
(1, 'Arabica', 'Hat ca phe Arabica'),
(2, 'Robusta', 'Hat ca phe Robusta')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO suppliers (id, name) VALUES
(1, 'Da Lat Farm Cooperative'),
(2, 'Dak Lak Supplier Group')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO inventory_items (product_id, product_name, quantity_available, quantity_reserved, warehouse_id, warehouse_location, reorder_level) VALUES
('1', 'REVO Morning', 99250, 0, 1, 'BC2-WH-A1', 50),
('2', 'REVO Everyday', 99749, 0, 2, 'BC2-WH-A2', 50),
('3', 'REVO Origin', 99496, 0, 3, 'BC2-WH-A3', 50),
('4', 'REVO Dam Da', 99000, 0, 4, 'BC2-WH-A4', 50),
('5', 'REVO Robusta', 99249, 0, 5, 'BC2-WH-A5', 50);

INSERT INTO stock_movements (product_id, quantity, movement_type, reference_id, reference_type, notes) VALUES
('1', 99250, 'in', 'BC2-SEED-1', 'legacy_import', 'Initial stock imported from BC2.sql'),
('2', 99749, 'in', 'BC2-SEED-2', 'legacy_import', 'Initial stock imported from BC2.sql'),
('3', 99496, 'in', 'BC2-SEED-3', 'legacy_import', 'Initial stock imported from BC2.sql'),
('4', 99000, 'in', 'BC2-SEED-4', 'legacy_import', 'Initial stock imported from BC2.sql'),
('5', 99249, 'in', 'BC2-SEED-5', 'legacy_import', 'Initial stock imported from BC2.sql');

INSERT INTO raw_material_lots (raw_material_type_id, bean_type, supplier_id, supplier, quantity_kg, quantity_remaining_kg, received_date, expiration_date, origin_region, notes) VALUES
(1, 'Arabica', 1, 'Da Lat Farm Cooperative', 50.00, 50.00, '2026-05-15', '2026-11-15', 'Cau Dat, Da Lat', 'Seed raw material lot for UC08'),
(2, 'Robusta', 2, 'Dak Lak Supplier Group', 80.00, 80.00, '2026-05-15', '2026-11-15', 'Dak Lak', 'Seed raw material lot for UC08');
