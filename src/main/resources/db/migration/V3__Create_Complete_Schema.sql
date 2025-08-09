-- V3: Create complete database schema for all missing tables
-- This migration adds all remaining tables needed for full application functionality

-- Step 1: Create contraception_methods table
CREATE TABLE contraception_methods (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    contraception_type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    effectiveness DECIMAL(5,2),
    instructions TEXT,
    next_appointment DATE,
    is_active BOOLEAN DEFAULT TRUE,
    prescribed_by VARCHAR(255),
    additional_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 2: Create contraception_side_effects table
CREATE TABLE contraception_side_effects (
    contraception_id BIGINT NOT NULL,
    side_effect VARCHAR(255) NOT NULL,
    PRIMARY KEY (contraception_id, side_effect),
    FOREIGN KEY (contraception_id) REFERENCES contraception_methods(id) ON DELETE CASCADE
);

-- Step 3: Create side_effect_reports table
CREATE TABLE side_effect_reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    contraception_method_id BIGINT NOT NULL,
    side_effect_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT,
    started_at DATE NOT NULL,
    ended_at DATE,
    is_ongoing BOOLEAN DEFAULT TRUE,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (contraception_method_id) REFERENCES contraception_methods(id) ON DELETE CASCADE
);

-- Step 4: Create medications table
CREATE TABLE medications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    prescribed_by VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 5: Create menstrual_cycles table
CREATE TABLE menstrual_cycles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cycle_start_date DATE NOT NULL,
    cycle_end_date DATE,
    flow_intensity VARCHAR(20),
    symptoms TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 6: Create education_lessons table
CREATE TABLE education_lessons (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100),
    difficulty_level VARCHAR(20),
    estimated_duration INTEGER,
    is_published BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Step 7: Create education_progress table
CREATE TABLE education_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    progress_percentage DECIMAL(5,2) DEFAULT 0.0,
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES education_lessons(id) ON DELETE CASCADE,
    UNIQUE(user_id, lesson_id)
);

-- Step 8: Create pregnancy_plans table
CREATE TABLE pregnancy_plans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_type VARCHAR(50) NOT NULL,
    target_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 9: Create partner_invitations table
CREATE TABLE partner_invitations (
    id BIGSERIAL PRIMARY KEY,
    inviter_id BIGINT NOT NULL,
    invitee_email VARCHAR(255) NOT NULL,
    invitation_code VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (inviter_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 10: Create partner_decisions table
CREATE TABLE partner_decisions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    decision_type VARCHAR(50) NOT NULL,
    decision VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 11: Create support_groups table
CREATE TABLE support_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    max_members INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Step 12: Create support_group_members table
CREATE TABLE support_group_members (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(20) DEFAULT 'MEMBER',
    FOREIGN KEY (group_id) REFERENCES support_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(group_id, user_id)
);

-- Step 13: Create support_tickets table
CREATE TABLE support_tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    ticket_type VARCHAR(50),
    priority VARCHAR(20) DEFAULT 'NORMAL',
    status VARCHAR(20) DEFAULT 'OPEN',
    assigned_to BIGINT,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
);

-- Step 14: Create forum_topics table
CREATE TABLE forum_topics (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100),
    author_id BIGINT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 15: Create notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 16: Create user_settings table
CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    language VARCHAR(10) DEFAULT 'en',
    theme VARCHAR(20) DEFAULT 'light',
    notification_preferences TEXT,
    privacy_settings TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 17: Create file_uploads table
CREATE TABLE file_uploads (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    category VARCHAR(50),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 18: Create time_slots table
CREATE TABLE time_slots (
    id BIGSERIAL PRIMARY KEY,
    health_facility_id BIGINT NOT NULL,
    health_worker_id BIGINT,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    day_of_week INTEGER NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (health_facility_id) REFERENCES health_facilities(id) ON DELETE CASCADE,
    FOREIGN KEY (health_worker_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Step 19: Create indexes for performance
CREATE INDEX idx_contraception_methods_user_id ON contraception_methods(user_id);
CREATE INDEX idx_contraception_methods_type ON contraception_methods(contraception_type);
CREATE INDEX idx_side_effect_reports_user_id ON side_effect_reports(user_id);
CREATE INDEX idx_side_effect_reports_method_id ON side_effect_reports(contraception_method_id);
CREATE INDEX idx_medications_user_id ON medications(user_id);
CREATE INDEX idx_medications_active ON medications(is_active);
CREATE INDEX idx_menstrual_cycles_user_id ON menstrual_cycles(user_id);
CREATE INDEX idx_menstrual_cycles_start_date ON menstrual_cycles(cycle_start_date);
CREATE INDEX idx_education_progress_user_id ON education_progress(user_id);
CREATE INDEX idx_education_progress_lesson_id ON education_progress(lesson_id);
CREATE INDEX idx_pregnancy_plans_user_id ON pregnancy_plans(user_id);
CREATE INDEX idx_pregnancy_plans_status ON pregnancy_plans(status);
CREATE INDEX idx_partner_invitations_inviter_id ON partner_invitations(inviter_id);
CREATE INDEX idx_partner_invitations_code ON partner_invitations(invitation_code);
CREATE INDEX idx_support_groups_category ON support_groups(category);
CREATE INDEX idx_support_tickets_user_id ON support_tickets(user_id);
CREATE INDEX idx_support_tickets_status ON support_tickets(status);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_file_uploads_user_id ON file_uploads(user_id);
CREATE INDEX idx_time_slots_facility_id ON time_slots(health_facility_id);
CREATE INDEX idx_time_slots_available ON time_slots(is_available);

-- Step 20: Insert sample contraception methods
INSERT INTO contraception_methods (user_id, contraception_type, name, description, start_date, effectiveness, is_active) VALUES
(1, 'PILL', 'Combined Oral Contraceptive', 'Daily birth control pill', '2024-01-01', 99.0, true),
(1, 'CONDOM', 'Male Condom', 'Barrier method', '2024-01-15', 85.0, true),
(2, 'IUD', 'Copper IUD', 'Long-term reversible contraception', '2024-01-01', 99.0, true);

-- Step 21: Insert sample education lessons
INSERT INTO education_lessons (title, content, category, difficulty_level, estimated_duration, is_published) VALUES
('Understanding Your Menstrual Cycle', 'Learn about the phases of your menstrual cycle and what to expect...', 'REPRODUCTIVE_HEALTH', 'BEGINNER', 15, true),
('Contraception Methods Overview', 'Comprehensive guide to different contraception methods...', 'FAMILY_PLANNING', 'BEGINNER', 20, true),
('Pregnancy Planning Basics', 'Essential information for planning a healthy pregnancy...', 'PREGNANCY', 'INTERMEDIATE', 25, true);

-- Step 22: Insert sample support groups
INSERT INTO support_groups (name, description, category, max_members, is_active) VALUES
('Teen Health Support', 'Support group for teenagers navigating reproductive health', 'TEEN_HEALTH', 50, true),
('Pregnancy Planning Group', 'Group for couples planning pregnancy', 'PREGNANCY_PLANNING', 30, true),
('Contraception Discussion', 'Open discussion about contraception methods', 'FAMILY_PLANNING', 40, true); 