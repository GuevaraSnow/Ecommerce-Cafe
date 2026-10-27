-- Usuarios iniciales por rol
INSERT INTO tbl_usuarios (id, nombre, email, contrasena, telefono, fecha_nacimiento, rol, eliminado)
VALUES ('usr-caficultor-01', 'Carlos Caficultor', 'carlos@finca.com', '$2a$10$e8Za3K2R4N.sW.pG/9Gv.O9f1fF8jJ1h2K3L4M5N6O7P8Q9R0S1T2', '3001234567', '1985-05-12', 'CAFICULTOR', false);

INSERT INTO tbl_usuarios (id, nombre, email, contrasena, telefono, fecha_nacimiento, rol, eliminado)
VALUES ('usr-tostador-01', 'Teresa Tostadora', 'teresa@tueste.com', '$2a$10$e8Za3K2R4N.sW.pG/9Gv.O9f1fF8jJ1h2K3L4M5N6O7P8Q9R0S1T2', '3109876543', '1990-08-20', 'TOSTADOR', false);

INSERT INTO tbl_usuarios (id, nombre, email, contrasena, telefono, fecha_nacimiento, rol, eliminado)
VALUES ('usr-vendedor-01', 'Valeria Vendedora', 'valeria@derivados.com', '$2a$10$e8Za3K2R4N.sW.pG/9Gv.O9f1fF8jJ1h2K3L4M5N6O7P8Q9R0S1T2', '3205554433', '1995-12-01', 'VENDEDOR_DERIVADOS', false);

-- Fincas iniciales
INSERT INTO tbl_fincas (id, propietario_id, nombre, departamento, municipio, altitud_metros, eliminada_logicamente)
VALUES ('finca-001', 'usr-caficultor-01', 'Finca Villa Laura', 'Quindío', 'Calarcá', 1650.0, false);

-- Transformaciones iniciales
INSERT INTO tbl_transformaciones (id, lote_codigo, cantidad_entrada_valor, cantidad_entrada_unidad, merma_valor, cantidad_resultante_valor, cantidad_resultante_unidad, cantidad_disponible_valor, cantidad_disponible_unidad, tostador_id, fecha, perfil_tueste)
VALUES ('trans-001', 'LOT-2026-001', 50.0, 'kg', 0.18, 41.0, 'kg', 41.0, 'kg', 'usr-tostador-01', '2026-10-20', 'MEDIO');