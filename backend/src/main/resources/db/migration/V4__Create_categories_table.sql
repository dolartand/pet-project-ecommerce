CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id BIGINT,

    CONSTRAINT fk_categories_parent_id FOREIGN KEY (parent_id)
        REFERENCES categories(id) ON DELETE SET NULL
)