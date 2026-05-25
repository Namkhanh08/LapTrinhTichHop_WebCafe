SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS revo_orders
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
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

CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(12, 2) NOT NULL,
    max_discount DECIMAL(12, 2) NULL,
    min_order_value DECIMAL(12, 2) NULL,
    usage_limit INT NULL,
    used_count INT DEFAULT 0,
    payment_method VARCHAR(50) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS voucher_categories (
    voucher_id BIGINT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (voucher_id, category_id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS voucher_products (
    voucher_id BIGINT NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (voucher_id, product_id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255),
    user_email VARCHAR(255),
    user_name VARCHAR(255),
    grind_size_id INT NULL,
    frequency ENUM('weekly', 'biweekly') NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status ENUM('active', 'paused', 'cancelled') DEFAULT 'active',
    next_delivery_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    subscription_id BIGINT NULL,
    user_email VARCHAR(255),
    user_name VARCHAR(255),
    status ENUM('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    receiver_name VARCHAR(100) NULL,
    receiver_phone VARCHAR(20) NULL,
    receiver_email VARCHAR(100) NULL,
    shipping_province VARCHAR(100) NULL,
    shipping_district VARCHAR(100) NULL,
    shipping_ward VARCHAR(100) NULL,
    shipping_detail_address VARCHAR(255) NULL,
    shipping_address TEXT NULL,
    shipping_phone VARCHAR(20) NULL,
    shipping_note VARCHAR(255) NULL,
    payment_method VARCHAR(50) DEFAULT 'cod',
    payment_status ENUM('pending', 'paid', 'failed', 'refunded') DEFAULT 'pending',
    voucher_code VARCHAR(50) NULL,
    discount_amount DECIMAL(12, 2) DEFAULT 0.00,
    final_amount DECIMAL(12, 2) DEFAULT 0.00,
    notes TEXT,
    cancel_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE SET NULL,
    FOREIGN KEY (voucher_code) REFERENCES vouchers(code) ON DELETE SET NULL,
    INDEX idx_orders_status (status),
    INDEX idx_orders_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    roast_batch_id BIGINT NULL,
    product_name VARCHAR(255),
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    flavor_notes VARCHAR(255) NULL,
    grinding_option_id INT NULL,
    weight VARCHAR(100) NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item_flavor_notes (
    order_item_id BIGINT NOT NULL,
    flavor_note VARCHAR(100) NOT NULL,
    PRIMARY KEY (order_item_id, flavor_note),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    INDEX idx_order_item_flavor_notes_note (flavor_note)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_code VARCHAR(50),
    from_status ENUM('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled') NULL,
    to_status ENUM('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled') NOT NULL,
    note TEXT,
    changed_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    grinding_option_id INT NULL,
    flavor_notes VARCHAR(255) NULL,
    weight VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_item_flavor_notes (
    cart_item_id BIGINT NOT NULL,
    flavor_note VARCHAR(100) NOT NULL,
    PRIMARY KEY (cart_item_id, flavor_note),
    FOREIGN KEY (cart_item_id) REFERENCES cart_items(id) ON DELETE CASCADE,
    INDEX idx_cart_item_flavor_notes_note (flavor_note)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO discount_types (code, name, description) VALUES
('fixed', 'Fixed amount', 'Subtract a fixed amount from the order'),
('percentage', 'Percentage', 'Subtract a percentage from the order')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO payment_methods (code, name) VALUES
('ALL', 'All payment methods'),
('COD', 'Cash on delivery'),
('VNPAY', 'VNPAY')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO order_statuses (code, name, sort_order, is_terminal) VALUES
('pending', 'Pending', 10, FALSE),
('confirmed', 'Confirmed', 20, FALSE),
('processing', 'Processing', 30, FALSE),
('shipped', 'Shipped', 40, FALSE),
('delivered', 'Delivered', 50, TRUE),
('cancelled', 'Cancelled', 60, TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order), is_terminal = VALUES(is_terminal);

INSERT INTO payment_statuses (code, name, is_terminal) VALUES
('pending', 'Pending', FALSE),
('paid', 'Paid', TRUE),
('failed', 'Failed', TRUE),
('refunded', 'Refunded', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), is_terminal = VALUES(is_terminal);

INSERT INTO subscription_frequencies (code, name, interval_days) VALUES
('weekly', 'Weekly', 7),
('biweekly', 'Biweekly', 14)
ON DUPLICATE KEY UPDATE name = VALUES(name), interval_days = VALUES(interval_days);

INSERT INTO vouchers (id, code, title, description, discount_type, discount_value, max_discount, min_order_value, usage_limit, used_count, payment_method, is_active, start_date, end_date) VALUES
(1, 'REVOCAFE50', 'Giảm 50k cho đơn hàng đầu tiên', 'Áp dụng cho đơn hàng cà phê có giá trị từ 200k trở lên', 'fixed', 50000.00, 50000.00, 200000.00, 100, 0, 'ALL', TRUE, '2026-05-01 00:00:00', '2026-12-31 23:59:59'),
(2, 'COFFEELOVE', 'Giảm 10% tổng đơn hàng', 'Giảm tối đa 100k cho những tín đồ yêu thích cà phê Revo', 'percentage', 10.00, 100000.00, 100000.00, 500, 0, 'ALL', TRUE, '2026-05-01 00:00:00', '2026-12-31 23:59:59');

INSERT INTO orders (id, order_code, user_id, user_email, user_name, status, total_amount, receiver_name, receiver_phone, receiver_email, shipping_province, shipping_district, shipping_ward, shipping_detail_address, shipping_address, shipping_phone, shipping_note, payment_method, payment_status, voucher_code, discount_amount, final_amount, created_at) VALUES
(1, 'BC2-1', 2, 'user02@gmail.com', 'Vũ Nam Khánh', 'processing', 347000.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'COD', 'pending', NULL, 0.00, 347000.00, '2026-05-08 16:53:32'),
(2, 'BC2-2', 2, 'user02@gmail.com', 'Vũ Nam Khánh', 'processing', 427000.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'COD', 'pending', NULL, 0.00, 427000.00, '2026-05-08 16:59:45'),
(7, 'BC2-7', 2, 'user02@gmail.com', 'Vũ Nam Khánh', 'cancelled', 447000.00, 'Nam Khánh', '0989664863', NULL, 'Ninh Bình', 'Yên Mô', 'Yên Thành', NULL, NULL, NULL, 'No', 'COD', 'refunded', NULL, 0.00, 447000.00, '2026-05-09 23:52:27'),
(41, 'BC2-41', 1, 'user01@gmail.com', 'Nguyễn Văn User', 'pending', 218000.00, 'Nguyễn Văn User', '0911111111', NULL, 'Tỉnh Điện Biên', 'Thị xã Mường Lay', 'Xã Lay Nưa', 'haha', NULL, NULL, 'hehe', 'VNPAY', 'pending', NULL, 0.00, 218000.00, '2026-05-13 09:32:43'),
(58, 'BC2-58', 1, 'user01@gmail.com', 'Nguyễn Văn User', 'delivered', 298000.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'COD', 'paid', NULL, 0.00, 298000.00, '2026-05-14 23:20:34'),
(63, 'BC2-63', 2, 'user02@gmail.com', 'Vũ Nam Khánh', 'delivered', 198000.00, 'Vũ Nam Khánh', '0922222222', NULL, 'Tỉnh Quảng Ninh', 'Thị xã Quảng Yên', 'Phường Yên Hải', 'làng Liêm', NULL, NULL, 'giao trước 12h', 'COD', 'paid', NULL, 0.00, 198000.00, '2026-05-15 20:55:39');

INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, subtotal, flavor_notes, grinding_option_id, weight) VALUES
(1, 1, '1', 'REVO Morning', 2, 99000.00, 198000.00, 'Socola đen', NULL, NULL),
(2, 1, '3', 'REVO Origin', 1, 149000.00, 149000.00, 'Hạnh nhân rang', NULL, NULL),
(3, 2, '2', 'REVO Everyday', 2, 139000.00, 278000.00, 'Socola đen', NULL, NULL),
(4, 2, '3', 'REVO Origin', 1, 149000.00, 149000.00, 'Hạnh nhân rang', NULL, NULL),
(9, 7, '3', 'REVO Origin', 3, 149000.00, 447000.00, 'Hạt dẻ', NULL, NULL),
(47, 41, '5', 'REVO Robusta', 2, 109000.00, 218000.00, 'Caramel', 2, NULL),
(65, 58, '3', 'REVO Origin', 2, 149000.00, 298000.00, 'Gỗ sồi', 2, '250g'),
(71, 63, '1', 'REVO Morning', 2, 99000.00, 198000.00, 'Hoa nhài', 1, '250g');

INSERT INTO carts (id, user_id) VALUES
(1, 2),
(2, 1);

INSERT INTO cart_items (id, cart_id, product_id, quantity, grinding_option_id, flavor_notes, weight) VALUES
(70, 1, '2', 1, 5, 'Chocolate', '250g');

INSERT INTO order_item_flavor_notes (order_item_id, flavor_note)
SELECT id, TRIM(flavor_notes)
FROM order_items
WHERE flavor_notes IS NOT NULL AND TRIM(flavor_notes) <> ''
ON DUPLICATE KEY UPDATE flavor_note = VALUES(flavor_note);

INSERT INTO cart_item_flavor_notes (cart_item_id, flavor_note)
SELECT id, TRIM(flavor_notes)
FROM cart_items
WHERE flavor_notes IS NOT NULL AND TRIM(flavor_notes) <> ''
ON DUPLICATE KEY UPDATE flavor_note = VALUES(flavor_note);
