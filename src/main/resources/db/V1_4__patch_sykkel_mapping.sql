DELETE FROM hms_artnr_mapping WHERE source_hms_artnr = '315030' AND target_hms_artnr = '266313';
DELETE FROM hms_artnr_mapping WHERE source_hms_artnr = '266313' AND target_hms_artnr = '315030';

INSERT INTO hms_artnr_mapping (id, source_hms_artnr, target_hms_artnr) values
(gen_random_uuid(), '315030', '266314');