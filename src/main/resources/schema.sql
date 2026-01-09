CREATE TABLE IF NOT EXISTS business (
    business_id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    address TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS geo_index (
    business_id UUID PRIMARY KEY,
    geohash TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_geo_business FOREIGN KEY (business_id) REFERENCES business (business_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_geo_index_geohash ON geo_index (geohash);
