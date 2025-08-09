-- Create STI Test Records table
CREATE TABLE sti_test_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    test_type VARCHAR(50) NOT NULL,
    test_date DATE NOT NULL,
    test_location VARCHAR(255),
    test_provider VARCHAR(255),
    result_status VARCHAR(50) DEFAULT 'PENDING',
    result_date DATE,
    follow_up_required BOOLEAN DEFAULT FALSE,
    follow_up_date DATE,
    notes TEXT,
    is_confidential BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_sti_test_records_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_test_type 
        CHECK (test_type IN ('HIV', 'SYPHILIS', 'GONORRHEA', 'CHLAMYDIA', 'HEPATITIS_B', 'HERPES', 'COMPREHENSIVE')),
    
    CONSTRAINT chk_result_status 
        CHECK (result_status IN ('NEGATIVE', 'POSITIVE', 'INCONCLUSIVE', 'PENDING'))
);

-- Create indexes for better performance
CREATE INDEX idx_sti_test_records_user_id ON sti_test_records(user_id);
CREATE INDEX idx_sti_test_records_test_date ON sti_test_records(test_date);
CREATE INDEX idx_sti_test_records_result_status ON sti_test_records(result_status);
CREATE INDEX idx_sti_test_records_follow_up ON sti_test_records(follow_up_required, follow_up_date);

-- Insert sample STI test records for testing
-- Assuming user IDs 1, 2, 3 exist from previous test data

-- STI test records for user 1
INSERT INTO sti_test_records (user_id, test_type, test_date, test_location, test_provider, result_status, result_date, follow_up_required, follow_up_date, notes, is_confidential) VALUES
(1, 'HIV', '2024-01-15', 'Kigali University Teaching Hospital', 'Dr. Uwimana', 'NEGATIVE', '2024-01-18', false, null, 'Routine HIV screening - negative result', true),
(1, 'SYPHILIS', '2024-01-15', 'Kigali University Teaching Hospital', 'Dr. Uwimana', 'NEGATIVE', '2024-01-18', false, null, 'Syphilis test - negative result', true),
(1, 'COMPREHENSIVE', '2024-06-10', 'King Faisal Hospital', 'Dr. Mukamana', 'PENDING', null, true, '2024-08-10', 'Comprehensive STI panel - awaiting results', true),
(1, 'HIV', '2024-07-20', 'Nyagatare District Hospital', 'Dr. Niyonsenga', 'NEGATIVE', '2024-07-22', false, null, 'Follow-up HIV test - negative', true);

-- STI test records for user 2
INSERT INTO sti_test_records (user_id, test_type, test_date, test_location, test_provider, result_status, result_date, follow_up_required, follow_up_date, notes, is_confidential) VALUES
(2, 'HIV', '2024-03-05', 'Butaro Hospital', 'Dr. Gasana', 'NEGATIVE', '2024-03-08', false, null, 'Pre-pregnancy HIV screening', true),
(2, 'HEPATITIS_B', '2024-03-05', 'Butaro Hospital', 'Dr. Gasana', 'NEGATIVE', '2024-03-08', false, null, 'Hepatitis B screening', true),
(2, 'CHLAMYDIA', '2024-05-15', 'Rwamagana District Hospital', 'Dr. Uwimana', 'POSITIVE', '2024-05-18', true, '2024-08-15', 'Chlamydia positive - treatment started', true);

-- STI test records for user 3
INSERT INTO sti_test_records (user_id, test_type, test_date, test_location, test_provider, result_status, result_date, follow_up_required, follow_up_date, notes, is_confidential) VALUES
(3, 'HIV', '2024-02-20', 'Kibagabaga Hospital', 'Dr. Mukamana', 'NEGATIVE', '2024-02-23', false, null, 'Routine screening', true),
(3, 'GONORRHEA', '2024-04-10', 'Masaka District Hospital', 'Dr. Nzeyimana', 'NEGATIVE', '2024-04-12', false, null, 'Gonorrhea test - negative', true),
(3, 'COMPREHENSIVE', '2024-07-01', 'University Teaching Hospital of Kigali', 'Dr. Uwimana', 'PENDING', null, true, '2024-08-01', 'Annual comprehensive STI screening', true);
