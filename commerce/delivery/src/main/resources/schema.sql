CREATE TABLE IF NOT EXISTS deliveries (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    from_country VARCHAR(100),
    from_city VARCHAR(100),
    from_street VARCHAR(200),
    from_house VARCHAR(50),
    from_flat VARCHAR(50),
    to_country VARCHAR(100),
    to_city VARCHAR(100),
    to_street VARCHAR(200),
    to_house VARCHAR(50),
    to_flat VARCHAR(50),
    weight DOUBLE PRECISION,
    volume DOUBLE PRECISION,
    fragile BOOLEAN,
    status VARCHAR(50)
);

CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);