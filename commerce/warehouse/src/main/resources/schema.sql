CREATE TABLE IF NOT EXISTS products (
    product_id UUID PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    image_src VARCHAR(500),
    quantity_state VARCHAR(20) NOT NULL,
    product_state VARCHAR(20) NOT NULL,
    product_category VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 1)
);