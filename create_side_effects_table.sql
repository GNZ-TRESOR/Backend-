-- Create side effects table for contraception side effect reports
CREATE TABLE IF NOT EXISTS contraception_side_effects (
    id BIGSERIAL PRIMARY KEY,
    contraception_method_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    side_effect_name VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL CHECK (severity IN ('MILD', 'MODERATE', 'SEVERE')),
    frequency VARCHAR(50) NOT NULL CHECK (frequency IN ('RARE', 'OCCASIONAL', 'COMMON', 'FREQUENT')),
    description TEXT,
    date_reported DATE NOT NULL,
    is_resolved BOOLEAN DEFAULT FALSE,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    -- Foreign key constraints
    CONSTRAINT fk_side_effects_contraception_method 
        FOREIGN KEY (contraception_method_id) 
        REFERENCES contraception_methods(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_side_effects_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_side_effects_user_id ON contraception_side_effects(user_id);
CREATE INDEX IF NOT EXISTS idx_side_effects_method_id ON contraception_side_effects(contraception_method_id);
CREATE INDEX IF NOT EXISTS idx_side_effects_date_reported ON contraception_side_effects(date_reported);
CREATE INDEX IF NOT EXISTS idx_side_effects_severity ON contraception_side_effects(severity);
CREATE INDEX IF NOT EXISTS idx_side_effects_is_resolved ON contraception_side_effects(is_resolved);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_side_effects_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_side_effects_updated_at
    BEFORE UPDATE ON contraception_side_effects
    FOR EACH ROW
    EXECUTE FUNCTION update_side_effects_updated_at();

-- Insert some sample data for testing
INSERT INTO contraception_side_effects (
    contraception_method_id, 
    user_id, 
    side_effect_name, 
    severity, 
    frequency, 
    description, 
    date_reported
) VALUES 
(1, 1, 'Nausea', 'MILD', 'OCCASIONAL', 'Mild nausea in the morning', CURRENT_DATE - INTERVAL '5 days'),
(1, 1, 'Headaches', 'MODERATE', 'COMMON', 'Frequent headaches, especially in the evening', CURRENT_DATE - INTERVAL '3 days'),
(2, 2, 'Cramping', 'MILD', 'RARE', 'Occasional cramping during menstruation', CURRENT_DATE - INTERVAL '7 days'),
(2, 2, 'Irregular bleeding', 'MODERATE', 'FREQUENT', 'Spotting between periods', CURRENT_DATE - INTERVAL '2 days')
ON CONFLICT DO NOTHING;

COMMIT;
