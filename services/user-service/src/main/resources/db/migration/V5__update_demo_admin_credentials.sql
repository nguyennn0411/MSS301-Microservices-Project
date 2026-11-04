-- Update the local demo admin without modifying the already-applied V3 migration.
UPDATE users
SET email = 'admin@mss.com',
    full_name = 'MSS Studio Administrator',
    password_hash = '$2a$10$iFpz3Xn7xUlII7TMriO/guvFuWs5zXrQYyv8WlTbvTOp6dh/6iffK'
WHERE id = '70000000-0000-0000-0000-000000000099'
  AND role = 'ADMIN';
