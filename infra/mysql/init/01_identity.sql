CREATE DATABASE IF NOT EXISTS revo_identity;
USE revo_identity;

CREATE TABLE IF NOT EXISTS roles (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('customer', 'admin', 'barista', 'stock_manager') DEFAULT 'customer',
    loyalty_points INT DEFAULT 0,
    phone VARCHAR(20),
    address TEXT,
    legacy_user_id VARCHAR(450) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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

CREATE TABLE IF NOT EXISTS addresses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    city VARCHAR(100),
    district VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_addresses_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO roles (code, name, description) VALUES
('customer', 'Customer', 'Customer account'),
('admin', 'Admin', 'System administrator'),
('barista', 'Barista', 'Production and preparation staff'),
('stock_manager', 'Stock manager', 'Inventory management staff')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- BC2.sql user mapping:
-- 1 -> 3F2504E0-4F89-11D3-9A0C-0305E82C3301
-- 2 -> 3F2504E0-4F89-11D3-9A0C-0305E82C3302
-- 3 -> E1109672-0043-41E1-BC61-638064F6D799
-- Passwords: user01/user02 use user123, admin uses @admi123.
INSERT INTO users (id, full_name, email, password_hash, role, loyalty_points, phone, address, legacy_user_id, created_at) VALUES
(1, 'Nguyễn Văn User', 'user01@gmail.com', 'pbkdf2$akSLpjt53tCuxgYfMsJe0A==$693vlH1mXvxUVTSbYm/DMttqLA9EoXRDB3AkP0XJUx0=', 'customer', 0, '0911111111', 'Đà Lạt', '3F2504E0-4F89-11D3-9A0C-0305E82C3301', '2026-04-29 21:35:11'),
(2, 'Vũ Nam Khánh', 'user02@gmail.com', 'pbkdf2$akSLpjt53tCuxgYfMsJe0A==$693vlH1mXvxUVTSbYm/DMttqLA9EoXRDB3AkP0XJUx0=', 'customer', 0, '0922222222', 'Buôn Ma Thuột', '3F2504E0-4F89-11D3-9A0C-0305E82C3302', '2026-04-29 21:35:11'),
(3, 'Admin System', 'admin@basecore.com', 'pbkdf2$8GM7NdlpxQB8/YtL/5xg/w==$96gHnKG6NPJqRfk8RLhaMjNy5ZAWBucavGd1xnAldMg=', 'admin', 0, '0988888888', 'Hà Nội', 'E1109672-0043-41E1-BC61-638064F6D799', '2026-04-29 21:35:11'),
(4, 'Admin Revo', 'admin@revo.coffee', 'pbkdf2$8GM7NdlpxQB8/YtL/5xg/w==$96gHnKG6NPJqRfk8RLhaMjNy5ZAWBucavGd1xnAldMg=', 'admin', 0, '0901234567', 'HCMC', NULL, CURRENT_TIMESTAMP);

INSERT INTO addresses (user_id, address_line, city, district, is_default) VALUES
(1, 'Đà Lạt', 'Lâm Đồng', NULL, TRUE),
(2, 'Buôn Ma Thuột', 'Đắk Lắk', NULL, TRUE),
(3, 'Hà Nội', 'Hà Nội', NULL, TRUE);
