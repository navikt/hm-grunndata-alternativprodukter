UPDATE hms_artnr_mapping
SET source_hms_artnr = '0' || source_hms_artnr
WHERE LENGTH(source_hms_artnr) = 5;

UPDATE hms_artnr_mapping
SET target_hms_artnr = '0' || target_hms_artnr
WHERE LENGTH(target_hms_artnr) = 5;