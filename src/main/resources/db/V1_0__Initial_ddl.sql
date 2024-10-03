CREATE TABLE IF NOT EXISTS hms_artnr_mapping (
    id UUID NOT NULL PRIMARY KEY,
    source_hms_artnr VARCHAR(255) NOT NULL,
    target_hms_artnr VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (source_hms_artnr, target_hms_artnr)
);

CREATE INDEX idx_source_hms_artnr ON hms_artnr_mapping (source_hms_artnr);

CREATE TABLE IF NOT EXISTS file_import_history (
    id UUID NOT NULL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (filename)
);