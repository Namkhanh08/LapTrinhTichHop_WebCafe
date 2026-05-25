SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS revo_products
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE revo_products;

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    category_id INT,
    type VARCHAR(50),
    region VARCHAR(100),
    altitude VARCHAR(50),
    processing_method VARCHAR(100),
    roast_level VARCHAR(50) NOT NULL,
    flavor_notes VARCHAR(255),
    image_url VARCHAR(500),
    stock INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_type (type),
    INDEX idx_region (region),
    INDEX idx_roast_level (roast_level),
    INDEX idx_processing_method (processing_method),
    CHECK (price >= 0),
    CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS flavor_notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_flavor_notes (
    product_id VARCHAR(36) NOT NULL,
    flavor_note_id INT NOT NULL,
    PRIMARY KEY (product_id, flavor_note_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (flavor_note_id) REFERENCES flavor_notes(id) ON DELETE CASCADE,
    INDEX idx_product_flavor_notes_note (flavor_note_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS grind_sizes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_grind_sizes (
    product_id VARCHAR(36) NOT NULL,
    grind_size_id INT NOT NULL,
    PRIMARY KEY (product_id, grind_size_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (grind_size_id) REFERENCES grind_sizes(id) ON DELETE CASCADE,
    INDEX idx_product_grind_sizes_size (grind_size_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO categories (id, name, description) VALUES
(1, 'Arabica', 'Cà phê có hương thơm thanh nhẹ, vị chua dịu, hậu ngọt'),
(2, 'Blend', 'Sự phối trộn hài hòa giữa Arabica và Robusta'),
(3, 'Robusta', 'Cà phê đậm vị, mạnh mẽ, hàm lượng caffeine cao'),
(4, 'Fine Robusta', 'Robusta tuyển chọn chất lượng cao, vị sâu và cân bằng');

INSERT INTO products (id, name, description, price, category_id, type, region, altitude, processing_method, roast_level, flavor_notes, image_url, stock, is_active) VALUES
('1', 'REVO Morning', 'Hương vị nhẹ nhàng, tinh tế cho buổi sáng đầy năng lượng', 99000.00, 1, 'arabica', 'Cầu Đất, Đà Lạt', '<1000m', 'Washed', 'light', 'Hoa nhài, Cam vàng, Mật ong', '/assets/img/section2/image1.png', 99250, TRUE),
('2', 'REVO Everyday', 'Hương vị cân bằng dịu nhẹ, hậu vị ngọt kéo dài', 139000.00, 2, 'blend', 'Đắk Lắk & Đà Lạt', '1000-2000m', 'Honey', 'medium-dark', 'Chocolate, Hạnh nhân, Caramel', '/assets/img/section2/image2.png', 99749, TRUE),
('3', 'REVO Origin', 'Robusta nguyên bản mạnh mẽ, đậm vị truyền thống', 149000.00, 3, 'robusta', 'Buôn Ma Thuột', '1000-2000m', 'Natural', 'dark', 'Cacao đắng, Gỗ sồi, Hạt dẻ', '/assets/img/section2/image3.png', 99496, TRUE),
('4', 'REVO Đậm Đà', 'Hương vị robusta tuyển chọn với hậu vị sâu lắng', 129000.00, 4, 'fine-robusta', 'Gia Lai', '>2000m', 'Semi Washed', 'medium-dark', 'Socola đen, Mật mía, Hạnh nhân rang', '/assets/img/section2/image4.png', 99000, TRUE),
('5', 'REVO Robusta', 'Robusta cổ điển mạnh mẽ, phù hợp gu Việt', 109000.00, 3, 'robusta', 'Đắk Lắk', '>2000m', 'Natural', 'medium', 'Caramel, Chocolate sữa, Gỗ nhẹ', '/assets/img/section2/image5.png', 99249, TRUE);

INSERT INTO grind_sizes (id, name) VALUES
(1, 'Nguyên hạt'),
(2, 'Pha phin'),
(3, 'Espresso'),
(4, 'Cold Brew'),
(5, 'French Press');

INSERT INTO flavor_notes (name) VALUES
('Hoa nhài'), ('Cam vàng'), ('Mật ong'), ('Chocolate'), ('Hạnh nhân'), ('Caramel'),
('Cacao đắng'), ('Gỗ sồi'), ('Hạt dẻ'), ('Socola đen'), ('Mật mía'), ('Hạnh nhân rang'),
('Chocolate sữa'), ('Gỗ nhẹ');

INSERT INTO product_grind_sizes (product_id, grind_size_id) VALUES
('1', 1), ('1', 2), ('1', 3),
('2', 1), ('2', 2), ('2', 3), ('2', 4), ('2', 5),
('3', 1), ('3', 2), ('3', 4),
('4', 1), ('4', 2), ('4', 3),
('5', 1), ('5', 2);

INSERT INTO product_flavor_notes (product_id, flavor_note_id)
SELECT p.id, f.id
FROM products p
JOIN flavor_notes f ON FIND_IN_SET(f.name, REPLACE(p.flavor_notes, ', ', ','));
