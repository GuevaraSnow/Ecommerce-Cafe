-- Datos de prueba de Presentacion (los carga application.properties).
-- Los ids y el vendedor dueno de las cuatro primeras coinciden con data-compra.sql:
--   pres-cafe-1 -> usr-tostador   pres-cafe-2 -> usr-caficultor
--   pres-cafe-3 -> usr-tostador   pres-taza-1 -> usr-derivados
-- Las fechas de tueste y de descuento son relativas a hoy, para que los datos no se venzan con el tiempo.
-- Disponibles para la venta: las cuatro primeras y las seis ultimas (diez en total, para que el catalogo tenga
-- mas de una pagina de 8). Las otras cuatro no deben aparecer en el catalogo:
--   pres-vencida (tostada hace 45 dias, mas de 30), pres-pausada (INACTIVA), pres-agotada (sin stock)
--   y pres-eliminada (baja logica).
-- Las seis ultimas (pres-cafe-4, pres-cafe-5, pres-molido-1, pres-termo-1, pres-molinillo-1 y pres-filtro-1)
-- salen de lo que sobraba en LOT-2026-001, trans-001 y trans-003 (ver data-produccion.sql).
--   pres-cafe-4 y pres-cafe-2 -> usr-caficultor   pres-cafe-5 -> usr-tostador
--   pres-molido-1, pres-termo-1, pres-molinillo-1 y pres-filtro-1 -> usr-derivados

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
     NULL, 'algodon', 'Gorra bordada'),
    ('pres-cafe-4', 'TRAZABLE', 'usr-caficultor', 'Cafe verde Caturra especial', 'CAFE_VERDE', 30000, 'COP',
     40, 'kg', NULL, NULL, NULL, 'ACTIVA', FALSE, 'LOT-2026-001', NULL, NULL,
     NULL, NULL, NULL),
    ('pres-cafe-5', 'TRAZABLE', 'usr-tostador', 'Cafe tostado Calarca', 'CAFE_TOSTADO', 31000, 'COP',
     8, 'kg', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, 'trans-001', 'CLARO',
     (CURRENT_DATE - INTERVAL '3' DAY), NULL, NULL),
    ('pres-molido-1', 'TRAZABLE', 'usr-derivados', 'Cafe molido para filtro', 'DERIVADO_CONSUMIBLE', 22000, 'COP',
     1, 'kg', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, 'trans-003', NULL,
     NULL, NULL, NULL),
    ('pres-termo-1', 'MERCHANDISING', 'usr-derivados', 'Termo cafetero', 'MERCHANDISING', 52000, 'COP',
     35, 'unidad', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, NULL, NULL,
     NULL, 'acero inoxidable', 'Termo de acero para llevar el cafe caliente'),
    ('pres-molinillo-1', 'MERCHANDISING', 'usr-derivados', 'Molinillo manual', 'MERCHANDISING', 85000, 'COP',
     18, 'unidad', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, NULL, NULL,
     NULL, 'aluminio', 'Molinillo manual para moler el cafe al momento'),
    ('pres-filtro-1', 'MERCHANDISING', 'usr-derivados', 'Filtro de tela reutilizable', 'MERCHANDISING', 12000, 'COP',
     60, 'unidad', NULL, NULL, NULL, 'ACTIVA', FALSE, NULL, NULL, NULL,
     NULL, 'algodon', 'Filtro de tela para preparar cafe de filtrado');

INSERT INTO presentacion_galeria (presentacion_id, posicion, url, principal) VALUES
    ('pres-cafe-1', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928100/cafe-de-origen/ecerfdhjyffbj8kcw0zn.jpg', TRUE),
    ('pres-cafe-1', 1, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928101/cafe-de-origen/hozvtjmgaags7oduyi9b.jpg', FALSE),
    ('pres-cafe-2', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794934227/cafe-de-origen/wultui90cukeqlyjewvt.jpg', TRUE),
    ('pres-cafe-3', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928100/cafe-de-origen/igvz0id7nxywrwo37ove.jpg', TRUE),
    ('pres-taza-1', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928102/cafe-de-origen/qoouj2zxrb3mfkizrvqs.jpg', TRUE),
    ('pres-vencida', 0, 'http://img/vencida.png', TRUE),
    ('pres-pausada', 0, 'http://img/llavero.png', TRUE),
    ('pres-agotada', 0, 'http://img/camiseta.png', TRUE),
    ('pres-eliminada', 0, 'http://img/gorra.png', TRUE),
    ('pres-cafe-4', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794934227/cafe-de-origen/wultui90cukeqlyjewvt.jpg', TRUE),
    ('pres-cafe-5', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794928101/cafe-de-origen/hozvtjmgaags7oduyi9b.jpg', TRUE),
    ('pres-molido-1', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794934231/cafe-de-origen/zfuh5s3xznrc8ykemrtm.jpg', TRUE),
    ('pres-termo-1', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794934228/cafe-de-origen/v0nfbazbaap4uwugv7pg.jpg', TRUE),
    ('pres-molinillo-1', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794934230/cafe-de-origen/uiurw8cxq9auovbfnpgq.jpg', TRUE),
    ('pres-filtro-1', 0, 'https://res.cloudinary.com/n3l6zph2/image/upload/v1794934229/cafe-de-origen/byk5bgu1tfroelal2mjj.jpg', TRUE);

INSERT INTO presentacion_nota_cata (presentacion_id, posicion, descriptor) VALUES
    ('pres-cafe-1', 0, 'Chocolate'),
    ('pres-cafe-1', 1, 'Panela'),
    ('pres-cafe-3', 0, 'Caramelo'),
    ('pres-cafe-5', 0, 'Frutos rojos'),
    ('pres-cafe-5', 1, 'Miel');
