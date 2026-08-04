CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_cost DOUBLE PRECISION,
    delivery_cost DOUBLE PRECISION,
    tax DOUBLE PRECISION,
    total_cost DOUBLE PRECISION,
    status VARCHAR(50)
);