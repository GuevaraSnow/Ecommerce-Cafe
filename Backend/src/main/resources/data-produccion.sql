-- Datos de prueba de la produccion: Finca -> Lote -> Transformacion (los carga application.properties).
-- Van en ese orden porque cada uno apunta al anterior. Los usuarios estan en data-usuarios.sql.
--
-- La Finca finca-001 es de usr-caficultor, que tambien publica el cafe verde (pres-cafe-2). Las
-- Transformaciones las hace usr-tostador, el vendedor de las Presentaciones de cafe tostado.
--
-- La cantidad de un Lote y la cantidad disponible de una Transformacion son lo que queda despues de los
-- descuentos ya hechos:
--   LOT-2026-001: 500 kg registrados; 200 kg a pres-cafe-2 (cafe verde), 80 kg a trans-001 y 15 kg a
--                 trans-003. Quedan 205 kg.
--   LOT-2026-002: 100 kg registrados; 40 kg a trans-002. Quedan 60 kg.
--   LOT-2025-001: Lote de la cosecha anterior, sin descuentos (150 kg).
--   trans-001: 80 kg con 20 % de merma dan 64 kg; 50 kg a pres-cafe-1. Quedan 14 kg.
--   trans-002: 40 kg con 15 % de merma dan 34 kg; 30 kg a pres-cafe-3. Quedan 4 kg.
--   trans-003: 15 kg con 20 % de merma dan 12 kg; 10 kg a pres-vencida. Quedan 2 kg.
-- El Perfil de Tueste de cada Transformacion es el de la Presentacion que sale de ella (CLARO, OSCURO y MEDIO).
-- Los codigos de 2026 son consecutivos (001 y 002): el siguiente Lote que registre la API sera LOT-2026-003.

INSERT INTO tbl_fincas (id, propietario_id, nombre, departamento, municipio, altitud_metros, eliminada_logicamente) VALUES
    ('finca-001', 'usr-caficultor', 'Finca Villa Laura', 'Quindío', 'Calarcá', 1650.0, false);

INSERT INTO tbl_lotes (codigo, finca_id, cosecha_anio, cosecha_temporada, proceso_beneficio, variedad, estado,
                       cantidad_valor, cantidad_unidad, eliminado_logicamente) VALUES
    ('LOT-2026-001', 'finca-001', 2026, 'PRINCIPAL', 'Lavado', 'Caturra', 'VERDE', 205.0, 'kg', false),
    ('LOT-2026-002', 'finca-001', 2026, 'TRAVIESA', 'Honey', 'Castillo Naranjal', 'PERGAMINO', 60.0, 'kg', false),
    ('LOT-2025-001', 'finca-001', 2025, 'TRAVIESA', 'Natural', 'Bourbon Amarillo', 'PERGAMINO', 150.0, 'kg', false);

INSERT INTO tbl_transformaciones (id, lote_codigo, cantidad_entrada_valor, cantidad_entrada_unidad, merma_valor,
                                  cantidad_resultante_valor, cantidad_resultante_unidad,
                                  cantidad_disponible_valor, cantidad_disponible_unidad,
                                  tostador_id, fecha, perfil_tueste) VALUES
    ('trans-001', 'LOT-2026-001', 80.0, 'kg', 0.20, 64.0, 'kg', 14.0, 'kg', 'usr-tostador', '2026-10-20', 'CLARO'),
    ('trans-002', 'LOT-2026-002', 40.0, 'kg', 0.15, 34.0, 'kg', 4.0, 'kg', 'usr-tostador', '2026-10-22', 'OSCURO'),
    ('trans-003', 'LOT-2026-001', 15.0, 'kg', 0.20, 12.0, 'kg', 2.0, 'kg', 'usr-tostador', '2026-09-10', 'MEDIO');
