-- V1: Create complete database schema with user-centric health_records
-- This migration creates all necessary tables for the Ubuzima application

-- Step 1: Create users table first (required for foreign keys)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    gender VARCHAR(20),
    date_of_birth DATE,
    facility_id VARCHAR(255),
    district VARCHAR(255),
    sector VARCHAR(255),
    cell VARCHAR(255),
    village VARCHAR(255),
    emergency_contact VARCHAR(255),
    preferred_language VARCHAR(10) DEFAULT 'rw',
    profile_picture_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    last_login_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Step 2: Create user-centric health_records table
CREATE TABLE health_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    
    -- Heart rate columns
    heart_rate_value INT,
    heart_rate_unit VARCHAR(10) DEFAULT 'bpm',
    
    -- Blood pressure columns  
    bp_value VARCHAR(20),
    bp_unit VARCHAR(10) DEFAULT 'mmHg',
    
    -- Weight columns (kg_value, kg_unit as shown in image)
    kg_value DECIMAL(5,2),
    kg_unit VARCHAR(10) DEFAULT 'kg',
    
    -- Temperature columns
    temp_value DECIMAL(4,1),
    temp_unit VARCHAR(10) DEFAULT '°C',
    
    -- Additional useful columns
    height_value DECIMAL(5,2),
    height_unit VARCHAR(10) DEFAULT 'cm',
    
    -- Computed fields
    bmi DECIMAL(4,1),
    health_status VARCHAR(20) DEFAULT 'normal',
    
    -- Metadata
    notes TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    recorded_by VARCHAR(100),
    assigned_health_worker_id BIGINT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_health_worker_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Step 4: Create indexes for performance
CREATE INDEX idx_health_records_user_id ON health_records(user_id);
CREATE INDEX idx_health_records_health_worker ON health_records(assigned_health_worker_id);
CREATE INDEX idx_health_records_health_status ON health_records(health_status);

-- Step 5: Create other essential tables

-- Health Facilities table
CREATE TABLE health_facilities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    facility_type VARCHAR(50) NOT NULL,
    address VARCHAR(500) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    operating_hours VARCHAR(255),
    services_offered TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    website_url VARCHAR(500),
    emergency_contact VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Messages table
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    conversation_id VARCHAR(255),
    content TEXT,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Appointments table
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    health_facility_id BIGINT,
    appointment_date TIMESTAMP NOT NULL,
    appointment_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    notes TEXT,
    reminder_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (health_facility_id) REFERENCES health_facilities(id) ON DELETE SET NULL
);

-- Step 6: Insert sample data for testing
INSERT INTO users (name, email, phone, password_hash, role) VALUES
('John Doe', 'john@example.com', '+250781234567', '$2a$10$example', 'CLIENT'),
('Jane Smith', 'jane@example.com', '+250781234568', '$2a$10$example', 'CLIENT'),
('Dr. Alice', 'alice@hospital.rw', '+250781234569', '$2a$10$example', 'HEALTH_WORKER'),
('Admin User', 'admin@ubuzima.rw', '+250781234570', '$2a$10$example', 'ADMIN');
