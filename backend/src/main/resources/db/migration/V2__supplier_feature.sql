ALTER TABLE users
    ADD COLUMN supplier_since DATETIME NULL,
    ADD COLUMN supplier_profile VARCHAR(1000);

ALTER TABLE products
    ADD COLUMN supplier_id BIGINT NULL,
    ADD CONSTRAINT fk_products_supplier FOREIGN KEY (supplier_id) REFERENCES users(id);

CREATE TABLE IF NOT EXISTS supplier_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    applicant_id BIGINT NOT NULL,
    business_name VARCHAR(150) NOT NULL,
    business_email VARCHAR(255) NOT NULL,
    business_phone VARCHAR(50),
    website VARCHAR(255),
    message TEXT,
    status VARCHAR(20) NOT NULL,
    submitted_at DATETIME NOT NULL,
    reviewed_at DATETIME,
    reviewed_by VARCHAR(100),
    admin_note VARCHAR(500),
    CONSTRAINT fk_supplier_applicant FOREIGN KEY (applicant_id) REFERENCES users(id)
);

CREATE INDEX idx_supplier_applications_status
    ON supplier_applications(status);
