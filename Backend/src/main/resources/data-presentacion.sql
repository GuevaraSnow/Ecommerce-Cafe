-- Datos de prueba de Presentacion (los carga application.properties).
-- Los ids y el vendedor dueno de las cuatro primeras coinciden con data-compra.sql:
--   pres-cafe-1 -> usr-tostador   pres-cafe-2 -> usr-caficultor
--   pres-cafe-3 -> usr-tostador   pres-taza-1 -> usr-derivados
-- Las fechas de tueste y de descuento son relativas a hoy, para que los datos no se venzan con el tiempo.
-- Disponibles para la venta: las cuatro primeras. Las otras cuatro no deben aparecer en el catalogo:
--   pres-vencida (tostada hace 45 dias, mas de 30), pres-pausada (INACTIVA), pres-agotada (sin stock)
--   y pres-eliminada (baja logica).

INSERT INTO presentaciones (id, clase, vendedor_id, titulo, tipo, precio_monto, precio_moneda,
                            cantidad_valor, cantidad_unidad, descuento_porcentaje, descuento_fecha_inicio,
                            descuento_fecha_fin, estado, eliminada, lote_id, transformacion_id, perfil_tueste,
                            fecha_tueste, material, descripcion) VALUES
    ('pres-cafe-1', 'TRAZABLE', 'usr-tostador', 'Cafe tostado Huila', 'CAFE_TOSTADO', 28000, 'COP',
     50, 'kg', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, 'transformacion-1', 'CLARO',
     DATEADD('DAY', -5, CURRENT_DATE), NULL, NULL),
    ('pres-cafe-2', 'TRAZABLE', 'usr-caficultor', 'Cafe verde Quindio', 'CAFE_VERDE', 32000, 'COP',
     200, 'kg', NULL, NULL, NULL, 'ACTIVA', FALSE, 'LOT-2026-001', NULL, NULL,
     NULL, NULL, NULL),
    ('pres-cafe-3', 'TRAZABLE', 'usr-tostador', 'Cafe tostado Narino', 'CAFE_TOSTADO', 45000, 'COP',
     30, 'kg', 20, DATEADD('DAY', -5, CURRENT_DATE), DATEADD('DAY', 5, CURRENT_DATE), 'ACTIVA', FALSE,
     NULL, 'transformacion-2', 'OSCURO', DATEADD('DAY', -10, CURRENT_DATE), NULL, NULL),
    ('pres-taza-1', 'MERCHANDISING', 'usr-derivados', 'Taza de ceramica', 'MERCHANDISING', 15000, 'COP',
     40, 'unidad', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, NULL, NULL,
     NULL, 'ceramica', 'Taza artesanal para cafe'),
    ('pres-vencida', 'TRAZABLE', 'usr-tostador', 'Cafe tostado vencido', 'CAFE_TOSTADO', 26000, 'COP',
     10, 'kg', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, 'transformacion-1', 'MEDIO',
     DATEADD('DAY', -45, CURRENT_DATE), NULL, NULL),
    ('pres-pausada', 'MERCHANDISING', 'usr-derivados', 'Llavero cafetero', 'MERCHANDISING', 8000, 'COP',
     25, 'unidad', NULL, NULL, NULL, 'INACTIVA', FALSE, NULL, NULL, NULL,
     NULL, 'madera', 'Llavero de madera'),
    ('pres-agotada', 'MERCHANDISING', 'usr-derivados', 'Camiseta cafetera', 'MERCHANDISING', 45000, 'COP',
     0, 'unidad', NULL, NULL, NULL, 'AGOTADA', FALSE, NULL, NULL, NULL,
     NULL, 'algodon', 'Camiseta de algodon'),
    ('pres-eliminada', 'MERCHANDISING', 'usr-derivados', 'Gorra cafetera', 'MERCHANDISING', 30000, 'COP',
     12, 'unidad', NULL, NULL, NULL, 'ACTIVA', TRUE, NULL, NULL, NULL,
     NULL, 'algodon', 'Gorra bordada');

INSERT INTO presentacion_galeria (presentacion_id, posicion, url, principal) VALUES
    ('pres-cafe-1', 0, 'http://img/cafe-1-principal.png', TRUE),
    ('pres-cafe-1', 1, 'http://img/cafe-1-detalle.png', FALSE),
    ('pres-cafe-2', 0, 'http://img/cafe-2.png', TRUE),
    ('pres-cafe-3', 0, 'http://img/cafe-3.png', TRUE),
    ('pres-taza-1', 0, 'http://img/taza-1.png', TRUE),
    ('pres-taza-1', 1, 'http://img/taza-2.png', FALSE),
    ('pres-vencida', 0, 'http://img/vencida.png', TRUE),
    ('pres-pausada', 0, 'http://img/llavero.png', TRUE),
    ('pres-agotada', 0, 'http://img/camiseta.png', TRUE),
    ('pres-eliminada', 0, 'http://img/gorra.png', TRUE);

INSERT INTO presentacion_nota_cata (presentacion_id, posicion, descriptor) VALUES
    ('pres-cafe-1', 0, 'Chocolate'),
    ('pres-cafe-1', 1, 'Panela'),
    ('pres-cafe-3', 0, 'Caramelo');
