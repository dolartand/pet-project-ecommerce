CREATE TABLE inventory_history (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    available_before INTEGER NOT NULL,
    available_after INTEGER NOT NULL,
    reserved_before INTEGER,
    reserved_after INTEGER,
    order_id BIGINT,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_history_product_id FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE
);