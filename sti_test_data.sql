-- Insert sample STI test records for testing
-- Assuming user IDs 1, 2, 3 exist from previous test data

-- STI test records for user 1
INSERT INTO sti_test_records (user_id, test_type, test_date, test_location, test_provider, result_status, result_date, follow_up_required, follow_up_date, notes, is_confidential, created_at, updated_at) VALUES
(1, 'HIV', '2024-01-15', 'Kigali University Teaching Hospital', 'Dr. Uwimana', 'NEGATIVE', '2024-01-18', false, null, 'Routine HIV screening - negative result', true, NOW(), NOW()),
(1, 'SYPHILIS', '2024-01-15', 'Kigali University Teaching Hospital', 'Dr. Uwimana', 'NEGATIVE', '2024-01-18', false, null, 'Syphilis test - negative result', true, NOW(), NOW()),
(1, 'COMPREHENSIVE', '2024-06-10', 'King Faisal Hospital', 'Dr. Mukamana', 'PENDING', null, true, '2024-07-10', 'Comprehensive STI panel - awaiting results', true, NOW(), NOW()),
(1, 'HIV', '2024-07-20', 'Nyagatare District Hospital', 'Dr. Niyonsenga', 'NEGATIVE', '2024-07-22', false, null, 'Follow-up HIV test - negative', true, NOW(), NOW());

-- STI test records for user 2
INSERT INTO sti_test_records (user_id, test_type, test_date, test_location, test_provider, result_status, result_date, follow_up_required, follow_up_date, notes, is_confidential, created_at, updated_at) VALUES
(2, 'HIV', '2024-03-05', 'Butaro Hospital', 'Dr. Gasana', 'NEGATIVE', '2024-03-08', false, null, 'Pre-pregnancy HIV screening', true, NOW(), NOW()),
(2, 'HEPATITIS_B', '2024-03-05', 'Butaro Hospital', 'Dr. Gasana', 'NEGATIVE', '2024-03-08', false, null, 'Hepatitis B screening', true, NOW(), NOW()),
(2, 'CHLAMYDIA', '2024-05-15', 'Rwamagana District Hospital', 'Dr. Uwimana', 'POSITIVE', '2024-05-18', true, '2024-08-15', 'Chlamydia positive - treatment started', true, NOW(), NOW());

-- STI test records for user 3
INSERT INTO sti_test_records (user_id, test_type, test_date, test_location, test_provider, result_status, result_date, follow_up_required, follow_up_date, notes, is_confidential, created_at, updated_at) VALUES
(3, 'HIV', '2024-02-20', 'Kibagabaga Hospital', 'Dr. Mukamana', 'NEGATIVE', '2024-02-23', false, null, 'Routine screening', true, NOW(), NOW()),
(3, 'GONORRHEA', '2024-04-10', 'Masaka District Hospital', 'Dr. Nzeyimana', 'NEGATIVE', '2024-04-12', false, null, 'Gonorrhea test - negative', true, NOW(), NOW()),
(3, 'COMPREHENSIVE', '2024-07-01', 'University Teaching Hospital of Kigali', 'Dr. Uwimana', 'PENDING', null, true, '2024-08-01', 'Annual comprehensive STI screening', true, NOW(), NOW());
