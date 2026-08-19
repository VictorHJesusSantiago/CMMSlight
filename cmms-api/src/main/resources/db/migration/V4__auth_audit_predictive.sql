ALTER TABLE app_user ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '';

CREATE TABLE audit_log (
    id              BIGSERIAL PRIMARY KEY,
    entity_name     VARCHAR(100) NOT NULL,
    entity_id       BIGINT,
    action          VARCHAR(20) NOT NULL,
    performed_by_id BIGINT REFERENCES app_user(id),
    performed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    details         TEXT
);
CREATE INDEX idx_audit_entity ON audit_log(entity_name, entity_id);
CREATE INDEX idx_audit_performed_at ON audit_log(performed_at);

CREATE TABLE sensor_threshold_rule (
    id              BIGSERIAL PRIMARY KEY,
    asset_id        BIGINT REFERENCES asset(id),
    asset_type_id   BIGINT REFERENCES asset_type(id),
    sensor_type     VARCHAR(50) NOT NULL,
    min_value       NUMERIC(14,4),
    max_value       NUMERIC(14,4),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_threshold_target CHECK (asset_id IS NOT NULL OR asset_type_id IS NOT NULL)
);
CREATE INDEX idx_threshold_asset ON sensor_threshold_rule(asset_id);
CREATE INDEX idx_threshold_asset_type ON sensor_threshold_rule(asset_type_id);

CREATE TABLE sensor_alert (
    id                  BIGSERIAL PRIMARY KEY,
    sensor_reading_id   BIGINT NOT NULL REFERENCES sensor_reading(id),
    threshold_rule_id   BIGINT NOT NULL REFERENCES sensor_threshold_rule(id),
    work_order_id       BIGINT REFERENCES work_order(id),
    triggered_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sensor_alert_rule ON sensor_alert(threshold_rule_id);
