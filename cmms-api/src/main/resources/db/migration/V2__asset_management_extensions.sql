ALTER TABLE asset_type ADD COLUMN custom_attributes_schema JSONB;

ALTER TABLE asset ADD COLUMN warranty_provider VARCHAR(150);
ALTER TABLE asset ADD COLUMN warranty_expiration DATE;
ALTER TABLE asset ADD COLUMN warranty_terms TEXT;
ALTER TABLE asset ADD COLUMN estimated_lifespan_months INT;
ALTER TABLE asset ADD COLUMN acquisition_cost NUMERIC(14,2);
ALTER TABLE asset ADD COLUMN acquisition_date DATE;
ALTER TABLE asset ADD COLUMN custom_attributes JSONB;

CREATE TABLE asset_attachment (
    id              BIGSERIAL PRIMARY KEY,
    asset_id        BIGINT NOT NULL REFERENCES asset(id) ON DELETE CASCADE,
    file_name       VARCHAR(255) NOT NULL,
    stored_path     VARCHAR(500) NOT NULL,
    content_type    VARCHAR(150),
    size_bytes      BIGINT NOT NULL,
    category        VARCHAR(30) NOT NULL DEFAULT 'OTHER',
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_asset_attachment_asset ON asset_attachment(asset_id);

CREATE TABLE asset_location_history (
    id                  BIGSERIAL PRIMARY KEY,
    asset_id            BIGINT NOT NULL REFERENCES asset(id) ON DELETE CASCADE,
    previous_location   VARCHAR(150),
    new_location        VARCHAR(150) NOT NULL,
    moved_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    moved_by_id         BIGINT REFERENCES app_user(id),
    notes               TEXT
);
CREATE INDEX idx_asset_location_history_asset ON asset_location_history(asset_id, moved_at);
