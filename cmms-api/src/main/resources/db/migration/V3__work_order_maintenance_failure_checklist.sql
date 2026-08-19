ALTER TABLE work_order ADD COLUMN signed_by_name VARCHAR(150);
ALTER TABLE work_order ADD COLUMN signed_at TIMESTAMPTZ;
ALTER TABLE work_order ADD COLUMN reopened_from_id BIGINT REFERENCES work_order(id);

CREATE TABLE work_order_event (
    id              BIGSERIAL PRIMARY KEY,
    work_order_id   BIGINT NOT NULL REFERENCES work_order(id) ON DELETE CASCADE,
    event_type      VARCHAR(30) NOT NULL,
    message         TEXT NOT NULL,
    created_by_id   BIGINT REFERENCES app_user(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_wo_event_wo ON work_order_event(work_order_id, created_at);

CREATE TABLE work_order_attachment (
    id              BIGSERIAL PRIMARY KEY,
    work_order_id   BIGINT NOT NULL REFERENCES work_order(id) ON DELETE CASCADE,
    file_name       VARCHAR(255) NOT NULL,
    stored_path     VARCHAR(500) NOT NULL,
    content_type    VARCHAR(150),
    size_bytes      BIGINT NOT NULL,
    category        VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_wo_attachment_wo ON work_order_attachment(work_order_id);

ALTER TABLE failure_history ADD COLUMN classification VARCHAR(30) NOT NULL DEFAULT 'OTHER';
ALTER TABLE failure_history ADD COLUMN why_1 TEXT;
ALTER TABLE failure_history ADD COLUMN why_2 TEXT;
ALTER TABLE failure_history ADD COLUMN why_3 TEXT;
ALTER TABLE failure_history ADD COLUMN why_4 TEXT;
ALTER TABLE failure_history ADD COLUMN why_5 TEXT;

ALTER TABLE checklist_item ADD COLUMN item_type VARCHAR(20) NOT NULL DEFAULT 'YES_NO';
ALTER TABLE checklist_item ADD COLUMN options JSONB;
ALTER TABLE checklist_item ADD COLUMN required BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE work_order_checklist_result ADD COLUMN value TEXT;
