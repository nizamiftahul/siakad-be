-- Seed default admin user (username: admin, password: admin123).
-- Password must be changed on production; hashedPassword below is a BCrypt hash.
INSERT INTO public."User" (name, email, "hashedPassword", role, username, jenjang)
VALUES (
    'Administrator',
    'admin@siakad.local',
    '$2a$10$iCc9YPsoGCEX8AUkaqa75u46PwWYRM4GyzWU00uTfPD.hZenp0PoK',
    'KSatu',
    'admin',
    'SD'
)
ON CONFLICT (username) DO NOTHING;
