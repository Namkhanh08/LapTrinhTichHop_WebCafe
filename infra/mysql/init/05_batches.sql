SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS revo_batches
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE revo_batches;

CREATE TABLE IF NOT EXISTS package_sizes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL,
    weight_grams INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    CHECK (weight_grams > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_code VARCHAR(50) NOT NULL UNIQUE,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255),
    quantity INT NOT NULL,
    package_250g_count INT DEFAULT 0,
    package_500g_count INT DEFAULT 0,
    package_1000g_count INT DEFAULT 0,
    roast_date DATE NOT NULL,
    roast_level VARCHAR(50),
    origin_region VARCHAR(100),
    process_method VARCHAR(100),
    notes TEXT,
    status ENUM('roasting', 'cooling', 'quality_check', 'packaging', 'completed', 'rejected') DEFAULT 'roasting',
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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

INSERT INTO package_sizes (id, code, label, weight_grams) VALUES
(1, '250g', '250g', 250),
(2, '500g', '500g', 500),
(3, '1000g', '1000g', 1000)
ON DUPLICATE KEY UPDATE label = VALUES(label), weight_grams = VALUES(weight_grams);

INSERT INTO batches (id, batch_code, product_id, product_name, quantity, package_250g_count, package_500g_count, package_1000g_count, roast_date, roast_level, origin_region, process_method, notes, status, created_by) VALUES
(1, 'B20260515-ARA', '1', 'REVO Morning', 15, 20, 10, 5, '2026-05-15', 'Medium', 'Cầu Đất, Đà Lạt', 'Washed', 'Mẻ rang seed cho Arabica sáng vị', 'completed', 'Admin Revo'),
(2, 'B20260516-ROB', '3', 'REVO Origin', 20, 24, 12, 8, '2026-05-16', 'Dark', 'Buôn Ma Thuột', 'Natural', 'Mẻ rang robusta nền đậm', 'packaging', 'Admin Revo'),
(3, 'B20260517-BLD', '2', 'REVO Everyday', 30, 30, 20, 10, '2026-05-17', 'Medium Dark', 'Đắk Lắk & Đà Lạt', 'Honey', 'Blend cân bằng cho đơn định kỳ', 'quality_check', 'Admin Revo')
ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    quantity = VALUES(quantity),
    package_250g_count = VALUES(package_250g_count),
    package_500g_count = VALUES(package_500g_count),
    package_1000g_count = VALUES(package_1000g_count),
    roast_level = VALUES(roast_level),
    origin_region = VALUES(origin_region),
    process_method = VALUES(process_method),
    notes = VALUES(notes),
    status = VALUES(status),
    created_by = VALUES(created_by);

INSERT INTO batch_package_outputs (batch_id, package_size_id, package_count) VALUES
(1, 1, 20), (1, 2, 10), (1, 3, 5),
(2, 1, 24), (2, 2, 12), (2, 3, 8),
(3, 1, 30), (3, 2, 20), (3, 3, 10)
ON DUPLICATE KEY UPDATE package_count = VALUES(package_count);

CREATE TABLE IF NOT EXISTS batch_quality_checks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    check_date DATE NOT NULL,
    moisture_content DECIMAL(5, 2),
    bean_density DECIMAL(6, 2),
    color_score INT,
    defect_count INT DEFAULT 0,
    aroma_notes TEXT,
    passed BOOLEAN DEFAULT FALSE,
    checked_by VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


