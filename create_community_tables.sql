-- Create Community Tables for Ubuzima App

-- Support Groups Table
CREATE TABLE IF NOT EXISTS support_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    creator_id BIGINT NOT NULL,
    member_count INTEGER NOT NULL DEFAULT 0,
    max_members INTEGER,
    is_private BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    meeting_schedule VARCHAR(255),
    meeting_location VARCHAR(255),
    contact_info VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- Support Group Members Table
CREATE TABLE IF NOT EXISTS support_group_members (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_activity_at TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES support_groups(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(group_id, user_id)
);

-- Support Group Tags Table
CREATE TABLE IF NOT EXISTS support_group_tags (
    group_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    FOREIGN KEY (group_id) REFERENCES support_groups(id),
    PRIMARY KEY (group_id, tag)
);

-- Forum Topics Table
CREATE TABLE IF NOT EXISTS forum_topics (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    author_id BIGINT NOT NULL,
    view_count INTEGER NOT NULL DEFAULT 0,
    reply_count INTEGER NOT NULL DEFAULT 0,
    is_pinned BOOLEAN NOT NULL DEFAULT false,
    is_locked BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_activity_at TIMESTAMP,
    last_reply_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (last_reply_by) REFERENCES users(id)
);

-- Forum Topic Tags Table
CREATE TABLE IF NOT EXISTS forum_topic_tags (
    topic_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    FOREIGN KEY (topic_id) REFERENCES forum_topics(id),
    PRIMARY KEY (topic_id, tag)
);

-- Community Events Table
CREATE TABLE IF NOT EXISTS community_events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(100) NOT NULL,
    organizer_id BIGINT NOT NULL,
    event_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    location VARCHAR(255) NOT NULL,
    max_participants INTEGER,
    current_participants INTEGER NOT NULL DEFAULT 0,
    registration_required BOOLEAN NOT NULL DEFAULT true,
    registration_deadline TIMESTAMP,
    is_virtual BOOLEAN NOT NULL DEFAULT false,
    virtual_link VARCHAR(500),
    contact_info VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_cancelled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES users(id)
);

-- Insert Sample Community Data
INSERT INTO support_groups (name, description, category, creator_id, member_count, max_members, is_private, is_active, meeting_schedule, meeting_location, contact_info, created_at, updated_at) VALUES
('Itsinda ry''abagore biga kubana n''ubwiyunge', 'Support Group for Women Family Planning', 'Family Planning', 3, 156, 200, false, true, 'Ku wa gatatu buri cyumweru saa kumi n''ebyiri', 'Kimisagara Health Center', '+250788111222', NOW(), NOW()),
('Ikiganiro cy''ubuzima', 'Health Discussion Forum', 'Health', 2, 89, 150, false, true, 'Ku wa kane buri cyumweru saa kumi', 'Online Platform', 'ubuzima@forum.rw', NOW(), NOW()),
('Abana b''urubyiruko', 'Youth Health Group', 'Youth Health', 4, 67, 100, false, true, 'Ku wa mbere buri cyumweru saa kumi n''ebyiri', 'Nyarugenge Health Center', '+250788555666', NOW(), NOW()),
('Ababyeyi bashya', 'New Parents Support', 'Parenting', 3, 45, 80, false, true, 'Ku wa gatanu buri cyumweru saa kumi', 'Kacyiru Health Center', '+250788777888', NOW(), NOW()),
('Ubuzima bw''ubwoba', 'Mental Health Support', 'Mental Health', 2, 34, 60, false, true, 'Ku wa kabiri buri cyumweru saa kumi n''ebyiri', 'King Faisal Hospital', '+250788500000', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert Support Group Members
INSERT INTO support_group_members (group_id, user_id, role, joined_at, is_active, last_activity_at) VALUES
(1, 3, 'ADMIN', NOW() - INTERVAL '30 days', true, NOW() - INTERVAL '1 day'),
(1, 4, 'MODERATOR', NOW() - INTERVAL '25 days', true, NOW() - INTERVAL '2 days'),
(1, 5, 'MEMBER', NOW() - INTERVAL '20 days', true, NOW() - INTERVAL '3 days'),
(2, 2, 'ADMIN', NOW() - INTERVAL '35 days', true, NOW() - INTERVAL '1 day'),
(2, 3, 'MEMBER', NOW() - INTERVAL '28 days', true, NOW() - INTERVAL '4 days'),
(3, 4, 'ADMIN', NOW() - INTERVAL '40 days', true, NOW() - INTERVAL '2 days'),
(4, 3, 'ADMIN', NOW() - INTERVAL '15 days', true, NOW() - INTERVAL '1 day'),
(5, 2, 'ADMIN', NOW() - INTERVAL '45 days', true, NOW() - INTERVAL '3 days')
ON CONFLICT DO NOTHING;

-- Insert Forum Topics
INSERT INTO forum_topics (title, content, category, author_id, view_count, reply_count, is_pinned, is_locked, is_active, last_activity_at, created_at, updated_at) VALUES
('Itsinda ry''abagore biga kubana n''ubwiyunge', 'Itsinda ry''abagore biga kubana n''ubwiyunge', 'Family Planning', 3, 245, 23, true, false, true, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '10 days', NOW()),
('Ikiganiro cy''ubuzima bw''imyororokere', 'Ikiganiro gishya kuri ubuzima bw''imyororokere', 'Health', 2, 189, 15, false, false, true, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '8 days', NOW()),
('Ubwiyunge bw''urubyiruko', 'Amakuru y''ubwiyunge bw''urubyiruko', 'Youth Health', 4, 156, 12, false, false, true, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 days', NOW()),
('Ababyeyi bashya - Inama', 'Inama z''ababyeyi bashya', 'Parenting', 3, 134, 18, false, false, true, NOW() - INTERVAL '8 hours', NOW() - INTERVAL '5 days', NOW()),
('Ubuzima bw''ubwoba - Ubufasha', 'Ubufasha mu buzima bw''ubwoba', 'Mental Health', 2, 98, 9, false, false, true, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '4 days', NOW())
ON CONFLICT DO NOTHING;

-- Insert Community Events
INSERT INTO community_events (title, description, type, organizer_id, event_date, end_date, location, max_participants, current_participants, registration_required, registration_deadline, is_virtual, virtual_link, contact_info, is_active, is_cancelled, created_at, updated_at) VALUES
('Ubwiyunge bw''urubyiruko', 'Youth Family Planning Workshop', 'WORKSHOP', 2, NOW() + INTERVAL '7 days', NOW() + INTERVAL '7 days' + INTERVAL '3 hours', 'Kimisagara Health Center', 50, 8, true, NOW() + INTERVAL '5 days', false, null, '+250788111222', true, false, NOW(), NOW()),
('Inama y''ubuzima', 'Health Education Workshop', 'SEMINAR', 3, NOW() + INTERVAL '14 days', NOW() + INTERVAL '14 days' + INTERVAL '4 hours', 'Nyarugenge Health Center', 100, 23, true, NOW() + INTERVAL '10 days', false, null, '+250788555666', true, false, NOW(), NOW()),
('Itsinda ry''abagore', 'Women Support Meeting', 'SUPPORT_MEETING', 4, NOW() + INTERVAL '21 days', NOW() + INTERVAL '21 days' + INTERVAL '2 hours', 'Kacyiru Health Center', 30, 15, true, NOW() + INTERVAL '18 days', false, null, '+250788777888', true, false, NOW(), NOW()),
('Ubuzima bw''abana', 'Child Health Camp', 'HEALTH_CAMP', 2, NOW() + INTERVAL '28 days', NOW() + INTERVAL '28 days' + INTERVAL '6 hours', 'King Faisal Hospital', 200, 45, true, NOW() + INTERVAL '25 days', false, null, '+250788500000', true, false, NOW(), NOW())
ON CONFLICT DO NOTHING;
