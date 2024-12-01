CREATE DATABASE IF NOT EXISTS blackfriday;
USE blackfriday;

-- Categories 테이블
CREATE TABLE categories (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    depth INT NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- Category Closure 테이블 (계층 구조 관리)
CREATE TABLE category_closure (
    closure_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ancestor_id BIGINT NOT NULL,
    descendant_id BIGINT NOT NULL,
    depth INT NOT NULL,
    FOREIGN KEY (ancestor_id) REFERENCES categories(category_id),
    FOREIGN KEY (descendant_id) REFERENCES categories(category_id),
    CONSTRAINT unique_ancestor_descendant UNIQUE (ancestor_id, descendant_id)
);

-- Products 테이블
CREATE TABLE products (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    stock_quantity INT NOT NULL,
    reserved_stock_quantity INT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL,
    category_id BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- 인덱스 추가
CREATE INDEX idx_category_is_deleted ON categories(is_deleted);
CREATE INDEX idx_products_is_deleted ON products(is_deleted);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_category_closure_ancestor ON category_closure(ancestor_id);
CREATE INDEX idx_category_closure_descendant ON category_closure(descendant_id);
