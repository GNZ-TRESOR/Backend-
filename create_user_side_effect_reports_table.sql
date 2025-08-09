-- Create a new table for user-reported side effects
-- This is separate from the existing contraception_side_effects table which stores predefined side effects

CREATE TABLE IF NOT EXISTS public.user_side_effect_reports
(
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
    CONSTRAINT fk_user_side_effects_contraception_method 
        FOREIGN KEY (contraception_method_id) 
        REFERENCES public.contraception_methods(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_user_side_effects_user 
        FOREIGN KEY (user_id) 
        REFERENCES public.users(id) 
        ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_user_side_effects_user_id ON public.user_side_effect_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_user_side_effects_method_id ON public.user_side_effect_reports(contraception_method_id);
CREATE INDEX IF NOT EXISTS idx_user_side_effects_date_reported ON public.user_side_effect_reports(date_reported);
CREATE INDEX IF NOT EXISTS idx_user_side_effects_severity ON public.user_side_effect_reports(severity);
CREATE INDEX IF NOT EXISTS idx_user_side_effects_is_resolved ON public.user_side_effect_reports(is_resolved);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_user_side_effects_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_user_side_effects_updated_at
    BEFORE UPDATE ON public.user_side_effect_reports
    FOR EACH ROW
    EXECUTE FUNCTION update_user_side_effects_updated_at();

-- Set table owner
ALTER TABLE IF EXISTS public.user_side_effect_reports OWNER to postgres;

-- Insert some sample data for testing (adjust IDs based on your existing data)
INSERT INTO public.user_side_effect_reports (
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

-- Note: The existing contraception_side_effects table can remain as is for storing 
-- predefined/common side effects for each contraception method.
-- This new table (user_side_effect_reports) stores actual user reports with detailed information.
