-- ======================================================================
-- Discussion Forum - Database Initialization Script // Diskuzní fórum - Inicializační skript databáze
-- ======================================================================
-- @author Petr Reitinger
-- @version 1.0
-- @since 2025
-- 
-- Purpose: Initialize database with default roles, admin user, and sample communities
-- Účel: Inicializovat databázi s výchozími rolemi, administrátorským uživatelem a vzorovými komunitami

-- User Roles Initialization // Inicializace uživatelských rolí
-- Insert default system roles for access control // Vložit výchozí systémové role pro řízení přístupu
MERGE INTO roles (name) VALUES ('ROLE_ADMIN');      -- Administrator role // Role administrátora
MERGE INTO roles (name) VALUES ('ROLE_USER');       -- Regular user role // Role běžného uživatele
MERGE INTO roles (name) VALUES ('ROLE_MODERATOR');  -- Moderator role // Role moderátora

-- Default Admin User Creation // Vytvoření výchozího administrátorského uživatele
-- Create administrator account with encrypted password // Vytvořit administrátorský účet se šifrovaným heslem
-- Password: admin123 (BCrypt encrypted) // Heslo: admin123 (šifrované BCrypt)
MERGE INTO users (username, password, display_name, enabled) 
VALUES ('admin', '$2a$12$DqMylWGF.G8ZtDZtwRiREe76etJx/8ARXLVrgip8mYnDW5vfRwlde', 'Administrator', true);

-- Admin Role Assignment // Přiřazení administrátorské role
-- Assign administrator privileges to admin user // Přiřadit administrátorská oprávnění uživateli admin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- Sample Communities Setup // Nastavení vzorových komunit
-- Create default community spaces for discussions // Vytvořit výchozí komunitní prostory pro diskuze

-- General discussion community // Komunita pro obecnou diskuzi
MERGE INTO communities (name, description) 
VALUES ('general', 'General discussion about anything and everything');

-- Technology-focused community // Komunita zaměřená na technologie
MERGE INTO communities (name, description) 
VALUES ('technology', 'Discussions about technology, programming, and software development');

-- Official announcements community // Komunita pro oficiální oznámení
MERGE INTO communities (name, description) 
VALUES ('announcements', 'Official announcements and updates');