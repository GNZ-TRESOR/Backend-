-- Alter the existing contraception_side_effects table to include additional fields
-- This will transform it from a simple collection table to a full user reports table

-- First, add the new columns
ALTER TABLE public.contraception_side_effects 
ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY,
ADD COLUMN IF NOT EXISTS user_id BIGINT,
ADD COLUMN IF NOT EXISTS severity VARCHAR(50) CHECK (severity IN ('MILD', 'MODERATE', 'SEVERE')),
ADD COLUMN IF NOT EXISTS frequency VARCHAR(50) CHECK (frequency IN ('RARE', 'OCCASIONAL', 'COMMON', 'FREQUENT')),
ADD COLUMN IF NOT EXISTS description TEXT,
ADD COLUMN IF NOT EXISTS date_reported DATE DEFAULT CURRENT_DATE,
ADD COLUMN IF NOT EXISTS is_resolved BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS resolution_notes TEXT,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Rename the existing column to match our model
ALTER TABLE public.contraception_side_effects 
RENAME COLUMN side_effect TO side_effect_name;

-- Rename contraception_id to contraception_method_id for consistency
ALTER TABLE public.contraception_side_effects 
RENAME COLUMN contraception_id TO contraception_method_id;

-- Add foreign key constraint for user_id
ALTER TABLE public.contraception_side_effects 
ADD CONSTRAINT fk_side_effects_user 
FOREIGN KEY (user_id) 
REFERENCES public.users(id) 
ON DELETE CASCADE;

-- Update the existing foreign key constraint name for clarity
ALTER TABLE public.contraception_side_effects 
DROP CONSTRAINT IF EXISTS fk6w166uv1nhum5d1l8r1kx94sk;

ALTER TABLE public.contraception_side_effects 
ADD CONSTRAINT fk_side_effects_contraception_method 
FOREIGN KEY (contraception_method_id) 
REFERENCES public.contraception_methods(id) 
ON DELETE CASCADE;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_side_effects_user_id ON public.contraception_side_effects(user_id);
CREATE INDEX IF NOT EXISTS idx_side_effects_method_id ON public.contraception_side_effects(contraception_method_id);
CREATE INDEX IF NOT EXISTS idx_side_effects_date_reported ON public.contraception_side_effects(date_reported);
CREATE INDEX IF NOT EXISTS idx_side_effects_severity ON public.contraception_side_effects(severity);
CREATE INDEX IF NOT EXISTS idx_side_effects_is_resolved ON public.contraception_side_effects(is_resolved);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_contraception_side_effects_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_contraception_side_effects_updated_at
    BEFORE UPDATE ON public.contraception_side_effects
    FOR EACH ROW
    EXECUTE FUNCTION update_contraception_side_effects_updated_at();

-- Update existing records to have default values for new fields
-- (You may need to adjust this based on your existing data)
UPDATE public.contraception_side_effects 
SET 
    severity = 'MILD',
    frequency = 'OCCASIONAL',
    date_reported = CURRENT_DATE,
    is_resolved = FALSE,
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP,
    version = 0
WHERE severity IS NULL;

-- Make certain fields NOT NULL after setting default values
ALTER TABLE public.contraception_side_effects 
ALTER COLUMN side_effect_name SET NOT NULL,
ALTER COLUMN contraception_method_id SET NOT NULL,
ALTER COLUMN date_reported SET NOT NULL;

COMMIT;

-- Note: After running this script, you should update the backend entity 
-- to use table name "contraception_side_effects" instead of "user_side_effect_reports"
