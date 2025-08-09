-- Test data for Ubuzima appointment booking system

-- Insert Health Facilities (Hospitals, Health Centers, Clinics, Dispensaries, Pharmacies)
INSERT INTO health_facilities (name, facility_type, address, phone_number, email, latitude, longitude, operating_hours, services_offered, is_active, created_at, updated_at) VALUES
-- Hospitals
('Kigali University Teaching Hospital (CHUK)', 'HOSPITAL', 'KN 4 Ave, Nyarugenge, Kigali', '+250788333444', 'kuth@health.gov.rw', -1.9536, 30.0606, '24/7', 'Emergency, Surgery, Maternity, Pediatrics, Family Planning, Cardiology, Oncology', true, NOW(), NOW()),
('King Faisal Hospital', 'HOSPITAL', 'KG 544 St, Kacyiru, Gasabo, Kigali', '+250788500000', 'info@kfh.rw', -1.9355, 30.0928, '24/7', 'Specialized Care, Surgery, Maternity, Family Planning, Cardiology, Neurology', true, NOW(), NOW()),
('Rwanda Military Hospital', 'HOSPITAL', 'KN 5 Rd, Kanombe, Kicukiro, Kigali', '+250788600000', 'rmh@health.gov.rw', -1.9667, 30.1394, '24/7', 'Emergency, Surgery, Maternity, General Medicine, Family Planning', true, NOW(), NOW()),

-- Health Centers
('Kimisagara Health Center', 'HEALTH_CENTER', 'Kimisagara, Nyarugenge, Kigali', '+250788111222', 'kimisagara@health.gov.rw', -1.9441, 30.0619, '08:00-17:00 (Mon-Fri), 08:00-12:00 (Sat)', 'Family Planning, Maternal Health, Child Health, General Medicine, Vaccination', true, NOW(), NOW()),
('Nyarugenge Health Center', 'HEALTH_CENTER', 'Nyarugenge, Kigali', '+250788555666', 'nyarugenge@health.gov.rw', -1.9506, 30.0588, '08:00-17:00 (Mon-Fri), 08:00-12:00 (Sat)', 'Family Planning, Maternal Health, General Medicine, HIV Testing', true, NOW(), NOW()),
('Kacyiru Health Center', 'HEALTH_CENTER', 'Kacyiru, Gasabo, Kigali', '+250788777888', 'kacyiru@health.gov.rw', -1.9355, 30.0928, '08:00-17:00 (Mon-Fri), 08:00-12:00 (Sat)', 'Family Planning, Maternal Health, Child Health, General Medicine', true, NOW(), NOW()),

-- Private Clinics
('Polyclinique du Plateau', 'CLINIC', 'KN 3 Ave, Nyarugenge, Kigali', '+250788200000', 'info@polyclinic.rw', -1.9500, 30.0580, '08:00-18:00 (Mon-Sat)', 'General Medicine, Family Planning, Laboratory, Radiology, Dental Care', true, NOW(), NOW()),
('Clinic Galien', 'CLINIC', 'KG 11 Ave, Kigali', '+250788300000', 'contact@galien.rw', -1.9450, 30.0650, '08:00-17:00 (Mon-Fri), 08:00-13:00 (Sat)', 'General Medicine, Family Planning, Pediatrics, Gynecology', true, NOW(), NOW()),
('Clinic La Medicale', 'CLINIC', 'KG 9 Ave, Kigali', '+250788400000', 'info@lamedicale.rw', -1.9480, 30.0620, '07:00-19:00 (Mon-Sat)', 'General Medicine, Family Planning, Emergency Care, Laboratory', true, NOW(), NOW()),
('Clinic Fraternite', 'CLINIC', 'KN 2 Ave, Kigali', '+250788250000', 'fraternite@clinic.rw', -1.9520, 30.0590, '08:00-17:00 (Mon-Fri)', 'General Medicine, Family Planning, Maternal Health, Child Health', true, NOW(), NOW()),

-- Dispensaries
('Gikondo Dispensary', 'DISPENSARY', 'Gikondo, Kicukiro, Kigali', '+250788150000', 'gikondo@health.gov.rw', -1.9800, 30.0700, '08:00-16:00 (Mon-Fri)', 'Basic Health Care, Family Planning, First Aid, Vaccination', true, NOW(), NOW()),
('Remera Dispensary', 'DISPENSARY', 'Remera, Gasabo, Kigali', '+250788160000', 'remera@health.gov.rw', -1.9200, 30.1000, '08:00-16:00 (Mon-Fri)', 'Basic Health Care, Family Planning, Maternal Health', true, NOW(), NOW()),
('Muhima Dispensary', 'DISPENSARY', 'Muhima, Nyarugenge, Kigali', '+250788170000', 'muhima@health.gov.rw', -1.9600, 30.0500, '08:00-16:00 (Mon-Fri)', 'Basic Health Care, Family Planning, Child Health', true, NOW(), NOW()),

-- Pharmacies
('Pharmakina Kigali', 'PHARMACY', 'KN 4 Ave, Kigali', '+250788350000', 'kigali@pharmakina.rw', -1.9500, 30.0600, '08:00-20:00 (Mon-Sun)', 'Prescription Drugs, Family Planning Products, Health Consultation', true, NOW(), NOW()),
('Pharmacy La Nouvelle', 'PHARMACY', 'KG 15 Ave, Kigali', '+250788360000', 'nouvelle@pharmacy.rw', -1.9450, 30.0680, '08:00-19:00 (Mon-Sat)', 'Prescription Drugs, Family Planning Products, Medical Supplies', true, NOW(), NOW()),
('Pharmacy du Peuple', 'PHARMACY', 'KN 1 Ave, Kigali', '+250788370000', 'peuple@pharmacy.rw', -1.9550, 30.0550, '08:00-18:00 (Mon-Sat)', 'Prescription Drugs, Family Planning Products, Health Consultation', true, NOW(), NOW());

-- Insert Support Groups
INSERT INTO support_groups (name, description, category, creator_id, member_count, max_members, is_private, is_active, meeting_schedule, meeting_location, contact_info, created_at, updated_at) VALUES
('Itsinda ry''abagore biga kubana n''ubwiyunge', 'Support Group', 'Family Planning', 3, 156, 200, false, true, 'Ku wa gatatu buri cyumweru saa kumi n''ebyiri', 'Kimisagara Health Center', '+250788111222', NOW(), NOW()),
('Ikiganiro cy''ubuzima', 'Forum', 'Health', 2, 89, 150, false, true, 'Ku wa kane buri cyumweru saa kumi', 'Online Platform', 'ubuzima@forum.rw', NOW(), NOW()),
('Abana b''urubyiruko', 'Youth Health Group', 'Youth Health', 4, 67, 100, false, true, 'Ku wa mbere buri cyumweru saa kumi n''ebyiri', 'Nyarugenge Health Center', '+250788555666', NOW(), NOW()),
('Ababyeyi bashya', 'New Parents Support', 'Parenting', 3, 45, 80, false, true, 'Ku wa gatanu buri cyumweru saa kumi', 'Kacyiru Health Center', '+250788777888', NOW(), NOW()),
('Ubuzima bw''ubwoba', 'Mental Health Support', 'Mental Health', 2, 34, 60, false, true, 'Ku wa kabiri buri cyumweru saa kumi n''ebyiri', 'King Faisal Hospital', '+250788500000', NOW(), NOW());

-- Insert Support Group Members
INSERT INTO support_group_members (group_id, user_id, role, joined_at, is_active, last_activity_at) VALUES
(1, 3, 'ADMIN', NOW() - INTERVAL '30 days', true, NOW() - INTERVAL '1 day'),
(1, 4, 'MODERATOR', NOW() - INTERVAL '25 days', true, NOW() - INTERVAL '2 days'),
(1, 5, 'MEMBER', NOW() - INTERVAL '20 days', true, NOW() - INTERVAL '3 days'),
(2, 2, 'ADMIN', NOW() - INTERVAL '35 days', true, NOW() - INTERVAL '1 day'),
(2, 3, 'MEMBER', NOW() - INTERVAL '28 days', true, NOW() - INTERVAL '4 days'),
(3, 4, 'ADMIN', NOW() - INTERVAL '40 days', true, NOW() - INTERVAL '2 days'),
(4, 3, 'ADMIN', NOW() - INTERVAL '15 days', true, NOW() - INTERVAL '1 day'),
(5, 2, 'ADMIN', NOW() - INTERVAL '45 days', true, NOW() - INTERVAL '3 days');

-- Insert Forum Topics
INSERT INTO forum_topics (title, content, category, author_id, view_count, reply_count, is_pinned, is_locked, is_active, last_activity_at, created_at, updated_at) VALUES
('Itsinda ry''abagore biga kubana n''ubwiyunge', 'Itsinda ry''abagore biga kubana n''ubwiyunge', 'Family Planning', 3, 245, 23, true, false, true, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '10 days', NOW()),
('Ikiganiro cy''ubuzima bw''imyororokere', 'Ikiganiro gishya kuri ubuzima bw''imyororokere', 'Health', 2, 189, 15, false, false, true, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '8 days', NOW()),
('Ubwiyunge bw''urubyiruko', 'Amakuru y''ubwiyunge bw''urubyiruko', 'Youth Health', 4, 156, 12, false, false, true, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 days', NOW()),
('Ababyeyi bashya - Inama', 'Inama z''ababyeyi bashya', 'Parenting', 3, 134, 18, false, false, true, NOW() - INTERVAL '8 hours', NOW() - INTERVAL '5 days', NOW()),
('Ubuzima bw''ubwoba - Ubufasha', 'Ubufasha mu buzima bw''ubwoba', 'Mental Health', 2, 98, 9, false, false, true, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '4 days', NOW());

-- Insert Community Events
INSERT INTO community_events (title, description, type, organizer_id, event_date, end_date, location, max_participants, current_participants, registration_required, registration_deadline, is_virtual, virtual_link, contact_info, is_active, is_cancelled, created_at, updated_at) VALUES
('Ubwiyunge bw''urubyiruko', 'Event', 'WORKSHOP', 2, NOW() + INTERVAL '7 days', NOW() + INTERVAL '7 days' + INTERVAL '3 hours', 'Kimisagara Health Center', 50, 8, true, NOW() + INTERVAL '5 days', false, null, '+250788111222', true, false, NOW(), NOW()),
('Inama y''ubuzima', 'Health Education Workshop', 'SEMINAR', 3, NOW() + INTERVAL '14 days', NOW() + INTERVAL '14 days' + INTERVAL '4 hours', 'Nyarugenge Health Center', 100, 23, true, NOW() + INTERVAL '10 days', false, null, '+250788555666', true, false, NOW(), NOW()),
('Itsinda ry''abagore', 'Women Support Meeting', 'SUPPORT_MEETING', 4, NOW() + INTERVAL '21 days', NOW() + INTERVAL '21 days' + INTERVAL '2 hours', 'Kacyiru Health Center', 30, 15, true, NOW() + INTERVAL '18 days', false, null, '+250788777888', true, false, NOW(), NOW()),
('Ubuzima bw''abana', 'Child Health Camp', 'HEALTH_CAMP', 2, NOW() + INTERVAL '28 days', NOW() + INTERVAL '28 days' + INTERVAL '6 hours', 'King Faisal Hospital', 200, 45, true, NOW() + INTERVAL '25 days', false, null, '+250788500000', true, false, NOW(), NOW());

-- Insert Health Workers
INSERT INTO users (name, email, phone, password, role, facility_id, is_active, created_at, updated_at) VALUES
('Dr. Uwimana Jean', 'uwimana@health.gov.rw', '+250788234567', '$2a$10$example_hash', 'HEALTH_WORKER', 1, true, NOW(), NOW()),
('Nurse Mukamana Marie', 'mukamana@health.gov.rw', '+250788345678', '$2a$10$example_hash', 'HEALTH_WORKER', 1, true, NOW(), NOW()),
('Dr. Nkurunziza Paul', 'nkurunziza@health.gov.rw', '+250788456789', '$2a$10$example_hash', 'HEALTH_WORKER', 2, true, NOW(), NOW()),
('Nurse Uwimana Grace', 'grace@health.gov.rw', '+250788567890', '$2a$10$example_hash', 'HEALTH_WORKER', 2, true, NOW(), NOW()),
('Dr. Mutesi Alice', 'mutesi@health.gov.rw', '+250788678901', '$2a$10$example_hash', 'HEALTH_WORKER', 3, true, NOW(), NOW()),
('Nurse Habimana John', 'habimana@health.gov.rw', '+250788789012', '$2a$10$example_hash', 'HEALTH_WORKER', 3, true, NOW(), NOW()),
('Dr. Kagame Eric', 'kagame@health.gov.rw', '+250788890123', '$2a$10$example_hash', 'HEALTH_WORKER', 4, true, NOW(), NOW()),
('Nurse Nyirahabimana Rose', 'rose@health.gov.rw', '+250788901234', '$2a$10$example_hash', 'HEALTH_WORKER', 4, true, NOW(), NOW());

-- Insert some test clients
INSERT INTO users (name, email, phone, password, role, is_active, created_at, updated_at) VALUES
('Uwimana Claudine', 'claudine@example.com', '+250788123456', '$2a$10$example_hash', 'CLIENT', true, NOW(), NOW()),
('Mukamana Jeanne', 'jeanne@example.com', '+250788234567', '$2a$10$example_hash', 'CLIENT', true, NOW(), NOW()),
('Niyonsenga Patrick', 'patrick@example.com', '+250788345678', '$2a$10$example_hash', 'CLIENT', true, NOW(), NOW());

-- Insert some time slots for testing
INSERT INTO time_slots (facility_id, health_worker_id, start_time, end_time, is_available, created_at, updated_at) VALUES
-- Kimisagara Health Center slots
(1, 1, '2024-01-15 09:00:00', '2024-01-15 09:30:00', true, NOW(), NOW()),
(1, 1, '2024-01-15 09:30:00', '2024-01-15 10:00:00', true, NOW(), NOW()),
(1, 1, '2024-01-15 10:00:00', '2024-01-15 10:30:00', true, NOW(), NOW()),
(1, 1, '2024-01-15 10:30:00', '2024-01-15 11:00:00', true, NOW(), NOW()),
(1, 2, '2024-01-15 14:00:00', '2024-01-15 14:30:00', true, NOW(), NOW()),
(1, 2, '2024-01-15 14:30:00', '2024-01-15 15:00:00', true, NOW(), NOW()),
(1, 2, '2024-01-15 15:00:00', '2024-01-15 15:30:00', true, NOW(), NOW()),

-- KUTH slots
(2, 3, '2024-01-15 08:00:00', '2024-01-15 08:30:00', true, NOW(), NOW()),
(2, 3, '2024-01-15 08:30:00', '2024-01-15 09:00:00', true, NOW(), NOW()),
(2, 3, '2024-01-15 09:00:00', '2024-01-15 09:30:00', true, NOW(), NOW()),
(2, 4, '2024-01-15 13:00:00', '2024-01-15 13:30:00', true, NOW(), NOW()),
(2, 4, '2024-01-15 13:30:00', '2024-01-15 14:00:00', true, NOW(), NOW()),
(2, 4, '2024-01-15 14:00:00', '2024-01-15 14:30:00', true, NOW(), NOW());
