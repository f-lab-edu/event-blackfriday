CREATE DATABASE IF NOT EXISTS blackfriday;
USE blackfriday;

-- Members 테이블
CREATE TABLE members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(60) NOT NULL,
    name VARCHAR(20) NOT NULL,
    membership_type ENUM('NORMAL', 'PRIME') NOT NULL DEFAULT 'NORMAL',
    membership_start_date DATETIME(6),
    membership_end_date DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- Categories 테이블
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    depth INT NOT NULL DEFAULT 1,
    seller_id BIGINT NOT NULL,
    display_order INT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (seller_id) REFERENCES members(member_id)
);

-- Category Closure 테이블 (계층 구조 관리)
CREATE TABLE category_closure (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ancestor_id BIGINT NOT NULL,
    descendant_id BIGINT NOT NULL,
    depth INT NOT NULL,
    FOREIGN KEY (ancestor_id) REFERENCES categories(category_id),
    FOREIGN KEY (descendant_id) REFERENCES categories(category_id),
    CONSTRAINT unique_ancestor_descendant UNIQUE (ancestor_id, descendant_id)
);

-- Products 테이블
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    stock_quantity INT NOT NULL,
    reserved_stock_quantity INT NOT NULL DEFAULT 0,
    seller_id BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL,
    category_id BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (seller_id) REFERENCES members(member_id)
);

-- Orders 테이블
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(255) NOT NULL UNIQUE,
    member_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    order_date_time DATETIME(6) NOT NULL,
    status_updated_at DATETIME(6) NOT NULL,
    timeout_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);

-- Payments 테이블
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_number VARCHAR(255) NOT NULL UNIQUE,
    order_number VARCHAR(255) NOT NULL,
    member_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    status_updated_at DATETIME(6) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);

-- 인덱스 추가
CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_categories_seller ON categories(seller_id);
CREATE INDEX idx_orders_member ON orders(member_id);
CREATE INDEX idx_payments_member ON payments(member_id);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_payments_order_number ON payments(order_number);
CREATE INDEX idx_payments_payment_number ON payments(payment_number);
CREATE INDEX idx_members_is_deleted ON members(is_deleted);
CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_category_is_deleted ON categories(is_deleted);
CREATE INDEX idx_products_is_deleted ON products(is_deleted);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_category_closure_ancestor ON category_closure(ancestor_id);
CREATE INDEX idx_category_closure_descendant ON category_closure(descendant_id);
