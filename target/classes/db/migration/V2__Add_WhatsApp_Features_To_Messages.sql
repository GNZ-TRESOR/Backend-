-- Add WhatsApp-like features to messages table
ALTER TABLE messages 
ADD COLUMN delivered_at TIMESTAMP,
ADD COLUMN audio_duration INTEGER,
ADD COLUMN audio_url VARCHAR(500),
ADD COLUMN file_size BIGINT,
ADD COLUMN mime_type VARCHAR(100),
ADD COLUMN thumbnail_url VARCHAR(500),
ADD COLUMN is_forwarded BOOLEAN DEFAULT FALSE,
ADD COLUMN forwarded_from VARCHAR(255),
ADD COLUMN edited_at TIMESTAMP,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_for_everyone BOOLEAN DEFAULT FALSE,
ADD COLUMN quoted_message_id BIGINT,
ADD COLUMN message_status VARCHAR(20) DEFAULT 'SENT',
ADD COLUMN reaction VARCHAR(10),
ADD COLUMN starred BOOLEAN DEFAULT FALSE;

-- Add foreign key constraint for quoted messages
ALTER TABLE messages 
ADD CONSTRAINT fk_quoted_message 
FOREIGN KEY (quoted_message_id) REFERENCES messages(id) ON DELETE SET NULL;

-- Add indexes for better performance
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at);
CREATE INDEX idx_messages_status ON messages(message_status);
CREATE INDEX idx_messages_audio_url ON messages(audio_url);
CREATE INDEX idx_messages_deleted_at ON messages(deleted_at);

-- Update existing messages to have proper status
UPDATE messages SET message_status = 'READ' WHERE is_read = true;
UPDATE messages SET message_status = 'delivered' WHERE is_read = false AND message_status = 'SENT';

-- Add missing fields to existing messages table if they don't exist
ALTER TABLE messages 
ADD COLUMN IF NOT EXISTS read_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS is_emergency BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS reply_to_id BIGINT;

-- Create message_attachments table if it doesn't exist
CREATE TABLE IF NOT EXISTS message_attachments (
    message_id BIGINT NOT NULL,
    attachment_url VARCHAR(500) NOT NULL,
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
);

-- Create typing_indicators table for real-time typing status
CREATE TABLE typing_indicators (
    id BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    is_typing BOOLEAN DEFAULT FALSE,
    last_typed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(conversation_id, user_id)
);

-- Create message_reactions table for emoji reactions
CREATE TABLE message_reactions (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(message_id, user_id)
);

-- Create conversation_participants table for group chats
CREATE TABLE conversation_participants (
    id BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,
    role VARCHAR(20) DEFAULT 'MEMBER', -- ADMIN, MEMBER
    is_muted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(conversation_id, user_id)
);

-- Create conversations table for better conversation management
CREATE TABLE conversations (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    is_group BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP,
    is_archived BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Add indexes for better performance
CREATE INDEX idx_typing_indicators_conversation ON typing_indicators(conversation_id);
CREATE INDEX idx_message_reactions_message ON message_reactions(message_id);
CREATE INDEX idx_conversation_participants_conversation ON conversation_participants(conversation_id);
CREATE INDEX idx_conversation_participants_user ON conversation_participants(user_id);
CREATE INDEX idx_conversations_updated_at ON conversations(updated_at);
