INSERT INTO app_user (name, email, password_hash, role, active)
VALUES ('Administrador', 'admin@cmmslight.local', '$2b$10$zEDu4hQSA6eFSF2zqXPnPuqOmYvv.JCu8hWChAjRVxKaylOH9ou5q', 'ADMIN', TRUE)
ON CONFLICT (email) DO NOTHING;
