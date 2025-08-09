-- Ubuzima Database Setup Script
-- Run this script as postgres superuser to create the database and user

-- Create the database
CREATE DATABASE ubuzima_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Create the user
CREATE USER ubuzima_user WITH
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    INHERIT
    NOREPLICATION
    CONNECTION LIMIT -1
    PASSWORD 'ubuzima_password';

-- Grant privileges on the database
GRANT ALL PRIVILEGES ON DATABASE ubuzima_db TO ubuzima_user;

-- Connect to the new database
\c ubuzima_db;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO ubuzima_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ubuzima_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ubuzima_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO ubuzima_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ubuzima_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ubuzima_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO ubuzima_user;

-- Create extensions that might be needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Verify the setup
SELECT 'Database setup completed successfully!' as status;
