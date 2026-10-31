-- Usuarios de prueba (los carga application.properties). Todos tienen la contrasena Cafe2026*, guardada
-- con BCrypt como la guarda la aplicacion, asi que se puede iniciar sesion con cualquiera de ellos.
-- Los ids son los que usan los demas datos de ejemplo: las Presentaciones, las Compras, las Resenas, la
-- Finca y las Transformaciones (ver data-produccion.sql, data-presentacion.sql y data-compra.sql).
--   Vendedores: usr-caficultor (Caficultor), usr-tostador (Tostador), usr-derivados (Vendedor de Derivados)
--   Compradores: usr-ana, usr-bruno, usr-carla, usr-diego

INSERT INTO tbl_usuarios (id, nombre, email, contrasena, telefono, fecha_nacimiento, rol, eliminado) VALUES
    ('usr-caficultor', 'Carlos Caficultor',  'carlos@finca.com',     '$2a$10$praaMQeqr2PZV0QD6iinDe6wg/exwKjbV8yD3djFaX85ytHyCTJQi', '3001234567', '1985-05-12', 'CAFICULTOR',        false),
    ('usr-tostador',   'Teresa Tostadora',  'teresa@tueste.com',    '$2a$10$praaMQeqr2PZV0QD6iinDe6wg/exwKjbV8yD3djFaX85ytHyCTJQi', '3109876543', '1990-08-20', 'TOSTADOR',          false),
    ('usr-derivados',  'Valeria Vendedora', 'valeria@derivados.com', '$2a$10$praaMQeqr2PZV0QD6iinDe6wg/exwKjbV8yD3djFaX85ytHyCTJQi', '3205554433', '1995-12-01', 'VENDEDOR_DERIVADOS', false),
    ('usr-ana',        'Ana Gomez',         'ana@correo.com',       '$2a$10$praaMQeqr2PZV0QD6iinDe6wg/exwKjbV8yD3djFaX85ytHyCTJQi', '3011112233', '1998-03-14', 'COMPRADOR',          false),
    ('usr-bruno',      'Bruno Rios',        'bruno@correo.com',     '$2a$10$praaMQeqr2PZV0QD6iinDe6wg/exwKjbV8yD3djFaX85ytHyCTJQi', '3022223344', '1992-07-09', 'COMPRADOR',          false),
    ('usr-carla',      'Carla Mejia',       'carla@correo.com',     '$2a$10$praaMQeqr2PZV0QD6iinDe6wg/exwKjbV8yD3djFaX85ytHyCTJQi', '3033334455', '2000-11-25', 'COMPRADOR',          false),
    ('usr-diego',      'Diego Arango',      'diego@correo.com',     '$2a$10$praaMQeqr2PZV0QD6iinDe6wg/exwKjbV8yD3djFaX85ytHyCTJQi', '3044445566', '1988-01-30', 'COMPRADOR',          false);
