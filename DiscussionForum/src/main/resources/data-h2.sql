-- H2 Database Data
-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles (name) VALUES ('ROLE_USER') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles (name) VALUES ('ROLE_MODERATOR') ON DUPLICATE KEY UPDATE name=name;

-- Insert admin user (password = admin123)
-- BCrypt hash for "admin123"
INSERT INTO users (username, password, display_name, enabled) 
VALUES ('admin', '$2a$12$DqMylWGF.G8ZtDZtwRiREe76etJx/8ARXLVrgip8mYnDW5vfRwlde', 'Administrator', true)
ON DUPLICATE KEY UPDATE username=username;

-- Assign ROLE_ADMIN to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- Insert sample communities
INSERT INTO communities (name, description) 
VALUES ('general', 'General discussion about anything and everything')
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO communities (name, description) 
VALUES ('technology', 'Discussions about technology, programming, and software development')
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO communities (name, description) 
VALUES ('announcements', 'Official announcements and updates')
ON DUPLICATE KEY UPDATE name=name;