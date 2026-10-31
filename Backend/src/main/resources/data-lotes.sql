-- Datos de prueba de Lote (los carga application.properties).
-- Todos los Lotes son de la finca-001 (propietario usr-caficultor-01, ver data-usuarios.sql).
-- La cantidad es lo que queda disponible tras los descuentos ya hechos:
--   LOT-2026-001: se registraron 500 kg; 200 kg fueron a la Presentacion pres-cafe-2 (cafe verde) y
--                 50 kg a la Transformacion trans-001, asi que quedan 250 kg.
--   LOT-2026-002: Lote nuevo, sin descuentos (100 kg).
--   LOT-2025-001: Lote de la cosecha anterior, sin descuentos (150 kg).
-- Los codigos de 2026 son consecutivos (001 y 002): el siguiente Lote que registre la API sera LOT-2026-003.

INSERT INTO tbl_lotes (codigo, finca_id, cosecha_anio, cosecha_temporada, proceso_beneficio, variedad, estado,
                       cantidad_valor, cantidad_unidad, eliminado_logicamente) VALUES
    ('LOT-2026-001', 'finca-001', 2026, 'PRINCIPAL', 'Lavado', 'Caturra', 'VERDE', 250.0, 'kg', false),
    ('LOT-2026-002', 'finca-001', 2026, 'TRAVIESA', 'Honey', 'Castillo Naranjal', 'PERGAMINO', 100.0, 'kg', false),
    ('LOT-2025-001', 'finca-001', 2025, 'TRAVIESA', 'Natural', 'Bourbon Amarillo', 'PERGAMINO', 150.0, 'kg', false);
