CREATE DATABASE IF NOT EXISTS revo_batches;
USE revo_batches;

CREATE TABLE IF NOT EXISTS batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_code VARCHAR(50) NOT NULL UNIQUE,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255),
    quantity INT NOT NULL,
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


