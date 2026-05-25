SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

USE revo_identity;

CREATE TABLE IF NOT EXISTS roles (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    points INT NOT NULL,
    reference_id VARCHAR(100),
    reference_type VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_loyalty_transactions_user_id (user_id),
    INDEX idx_loyalty_transactions_reference (reference_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO roles (code, name, description) VALUES
('customer', 'Customer', 'Customer account'),
('admin', 'Admin', 'System administrator'),
('barista', 'Barista', 'Production and preparation staff'),
('stock_manager', 'Stock manager', 'Inventory management staff')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

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

ALTER TABLE inventory_items
    ADD COLUMN IF NOT EXISTS warehouse_id INT NULL AFTER quantity_reserved;

ALTER TABLE raw_material_lots
    ADD COLUMN IF NOT EXISTS raw_material_type_id INT NULL AFTER id,
    ADD COLUMN IF NOT EXISTS supplier_id INT NULL AFTER bean_type,
    ADD COLUMN IF NOT EXISTS quantity_remaining_kg DECIMAL(10, 2) NOT NULL DEFAULT 0.00 AFTER quantity_kg;

UPDATE raw_material_lots
SET quantity_remaining_kg = quantity_kg
WHERE quantity_remaining_kg = 0;

USE revo_orders;

CREATE TABLE IF NOT EXISTS discount_types (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_methods (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_statuses (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_terminal BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_statuses (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_terminal BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS subscription_frequencies (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    interval_days INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item_flavor_notes (
    order_item_id BIGINT NOT NULL,
    flavor_note VARCHAR(100) NOT NULL,
    PRIMARY KEY (order_item_id, flavor_note),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    INDEX idx_order_item_flavor_notes_note (flavor_note)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_item_flavor_notes (
    cart_item_id BIGINT NOT NULL,
    flavor_note VARCHAR(100) NOT NULL,
    PRIMARY KEY (cart_item_id, flavor_note),
    FOREIGN KEY (cart_item_id) REFERENCES cart_items(id) ON DELETE CASCADE,
    INDEX idx_cart_item_flavor_notes_note (flavor_note)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE revo_batches;

CREATE TABLE IF NOT EXISTS package_sizes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL,
    weight_grams INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    CHECK (weight_grams > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS batch_package_outputs (
    batch_id BIGINT NOT NULL,
    package_size_id INT NOT NULL,
    package_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (batch_id, package_size_id),
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    FOREIGN KEY (package_size_id) REFERENCES package_sizes(id) ON DELETE RESTRICT,
    CHECK (package_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS batch_material_lots (
    batch_id BIGINT NOT NULL,
    raw_material_lot_id BIGINT NOT NULL,
    raw_material_name VARCHAR(100),
    quantity_kg DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (batch_id, raw_material_lot_id),
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    CHECK (quantity_kg > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
