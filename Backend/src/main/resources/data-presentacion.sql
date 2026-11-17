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
     50, 'kg', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, 'trans-001', 'CLARO',
     (CURRENT_DATE - INTERVAL '5' DAY), NULL, NULL),
    ('pres-cafe-2', 'TRAZABLE', 'usr-caficultor', 'Cafe verde Quindio', 'CAFE_VERDE', 32000, 'COP',
     200, 'kg', NULL, NULL, NULL, 'ACTIVA', FALSE, 'LOT-2026-001', NULL, NULL,
     NULL, NULL, NULL),
    ('pres-cafe-3', 'TRAZABLE', 'usr-tostador', 'Cafe tostado Narino', 'CAFE_TOSTADO', 45000, 'COP',
     30, 'kg', 20, (CURRENT_DATE - INTERVAL '5' DAY), (CURRENT_DATE + INTERVAL '5' DAY), 'ACTIVA', FALSE,
     NULL, 'trans-002', 'OSCURO', (CURRENT_DATE - INTERVAL '10' DAY), NULL, NULL),
    ('pres-taza-1', 'MERCHANDISING', 'usr-derivados', 'Taza de ceramica', 'MERCHANDISING', 15000, 'COP',
     40, 'unidad', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, NULL, NULL,
     NULL, 'ceramica', 'Taza artesanal para cafe'),
    ('pres-vencida', 'TRAZABLE', 'usr-tostador', 'Cafe tostado vencido', 'CAFE_TOSTADO', 26000, 'COP',
     10, 'kg', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, 'trans-003', 'MEDIO',
     (CURRENT_DATE - INTERVAL '45' DAY), NULL, NULL),
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
    ('pres-cafe-1', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928100/cafe-de-origen/ecerfdhjyffbj8kcw0zn.jpg', TRUE),
    ('pres-cafe-1', 1, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928101/cafe-de-origen/hozvtjmgaags7oduyi9b.jpg', FALSE),
    ('pres-cafe-2', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928101/cafe-de-origen/hozvtjmgaags7oduyi9b.jpg', TRUE),
    ('pres-cafe-3', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928100/cafe-de-origen/igvz0id7nxywrwo37ove.jpg', TRUE),
    ('pres-taza-1', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928102/cafe-de-origen/qoouj2zxrb3mfkizrvqs.jpg', TRUE),
    ('pres-vencida', 0, 'http://img/vencida.png', TRUE),
    ('pres-pausada', 0, 'http://img/llavero.png', TRUE),
    ('pres-agotada', 0, 'http://img/camiseta.png', TRUE),
    ('pres-eliminada', 0, 'http://img/gorra.png', TRUE);

INSERT INTO presentacion_nota_cata (presentacion_id, posicion, descriptor) VALUES
    ('pres-cafe-1', 0, 'Chocolate'),
    ('pres-cafe-1', 1, 'Panela'),
    ('pres-cafe-3', 0, 'Caramelo');
