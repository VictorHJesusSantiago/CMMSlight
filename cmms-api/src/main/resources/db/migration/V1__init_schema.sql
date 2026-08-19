CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    role            VARCHAR(30)  NOT NULL DEFAULT 'TECHNICIAN',
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE supplier (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    contact_name    VARCHAR(150),
    phone           VARCHAR(30),
    email           VARCHAR(150),
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE asset_type (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL UNIQUE,
    description     TEXT
);

CREATE TABLE asset (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    asset_type_id   BIGINT REFERENCES asset_type(id),
    parent_asset_id BIGINT REFERENCES asset(id),
    location        VARCHAR(150),
    manufacturer    VARCHAR(150),
    model           VARCHAR(150),
    serial_number   VARCHAR(150),
    install_date    DATE,
    status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    criticality     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_asset_type ON asset(asset_type_id);
CREATE INDEX idx_asset_parent ON asset(parent_asset_id);

CREATE TABLE checklist_template (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    description     TEXT
);

CREATE TABLE checklist_item (
    id                      BIGSERIAL PRIMARY KEY,
    checklist_template_id   BIGINT NOT NULL REFERENCES checklist_template(id) ON DELETE CASCADE,
    description             VARCHAR(255) NOT NULL,
    sort_order              INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_checklist_item_template ON checklist_item(checklist_template_id);

CREATE TABLE maintenance_plan (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(150) NOT NULL,
    asset_id                BIGINT REFERENCES asset(id),
    asset_type_id           BIGINT REFERENCES asset_type(id),
    checklist_template_id   BIGINT REFERENCES checklist_template(id),
    frequency_type          VARCHAR(20) NOT NULL,
    frequency_value         INT NOT NULL,
    frequency_unit          VARCHAR(20),
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    last_generated_at       TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_plan_target CHECK (asset_id IS NOT NULL OR asset_type_id IS NOT NULL)
);
CREATE INDEX idx_plan_asset ON maintenance_plan(asset_id);
CREATE INDEX idx_plan_asset_type ON maintenance_plan(asset_type_id);

CREATE TABLE work_order (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    asset_id            BIGINT NOT NULL REFERENCES asset(id),
    maintenance_plan_id BIGINT REFERENCES maintenance_plan(id),
    type                VARCHAR(20)  NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    priority            VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    requested_by_id     BIGINT REFERENCES app_user(id),
    assigned_to_id      BIGINT REFERENCES app_user(id),
    opened_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    scheduled_at        TIMESTAMPTZ,
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_wo_asset ON work_order(asset_id);
CREATE INDEX idx_wo_status ON work_order(status);
CREATE INDEX idx_wo_plan ON work_order(maintenance_plan_id);

CREATE TABLE work_order_checklist_result (
    id                  BIGSERIAL PRIMARY KEY,
    work_order_id       BIGINT NOT NULL REFERENCES work_order(id) ON DELETE CASCADE,
    checklist_item_id   BIGINT NOT NULL REFERENCES checklist_item(id),
    completed           BOOLEAN NOT NULL DEFAULT FALSE,
    notes               TEXT,
    UNIQUE (work_order_id, checklist_item_id)
);

CREATE TABLE failure_history (
    id                  BIGSERIAL PRIMARY KEY,
    asset_id            BIGINT NOT NULL REFERENCES asset(id),
    work_order_id       BIGINT REFERENCES work_order(id),
    failed_at           TIMESTAMPTZ NOT NULL,
    resolved_at         TIMESTAMPTZ,
    downtime_minutes    INT,
    description         TEXT,
    root_cause          TEXT
);
CREATE INDEX idx_failure_asset ON failure_history(asset_id);

CREATE TABLE part (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(50) NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    unit            VARCHAR(20) NOT NULL DEFAULT 'UN',
    quantity_on_hand NUMERIC(12,2) NOT NULL DEFAULT 0,
    min_quantity    NUMERIC(12,2) NOT NULL DEFAULT 0,
    supplier_id     BIGINT REFERENCES supplier(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE work_order_part (
    id              BIGSERIAL PRIMARY KEY,
    work_order_id   BIGINT NOT NULL REFERENCES work_order(id) ON DELETE CASCADE,
    part_id         BIGINT NOT NULL REFERENCES part(id),
    quantity_used   NUMERIC(12,2) NOT NULL,
    UNIQUE (work_order_id, part_id)
);

CREATE TABLE sensor_reading (
    id              BIGSERIAL PRIMARY KEY,
    asset_id        BIGINT NOT NULL REFERENCES asset(id),
    sensor_type     VARCHAR(50) NOT NULL,
    value           NUMERIC(14,4) NOT NULL,
    unit            VARCHAR(20),
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sensor_asset_time ON sensor_reading(asset_id, recorded_at);
