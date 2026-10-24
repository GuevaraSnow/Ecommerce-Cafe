-- Datos de prueba del vertical de Compra, Reembolso y Resena (los carga application.properties).
-- Presentaciones (por id) y su vendedor dueno:
--   pres-cafe-1 -> usr-tostador     pres-cafe-2 -> usr-caficultor
--   pres-cafe-3 -> usr-tostador     pres-taza-1 -> usr-derivados
-- Una compra en cada estado; los reembolsos y las resenas respetan las reglas del dominio
-- (resena solo con compra ENTREGADA, reembolso dentro de las 48 h, REEMBOLSADA con todo aprobado).

INSERT INTO compras (id, comprador_id, fecha_compra, estado, envio_ciudad, envio_direccion, envio_destinatario, fecha_entrega) VALUES
    ('compra-001', 'usr-ana',   '2026-10-08', 'ENTREGADA',   'Armenia',    'Cra 14 # 10-20',   'Ana Gomez',     '2026-10-10 15:00:00'),
    ('compra-002', 'usr-ana',   '2026-10-21', 'PENDIENTE',   NULL,         NULL,               NULL,            NULL),
    ('compra-003', 'usr-bruno', '2026-10-19', 'CONFIRMADA',  'Montenegro', 'Calle 10 # 5-20',  'Bruno Rios',    NULL),
    ('compra-004', 'usr-bruno', '2026-10-15', 'ENVIADA',     'Montenegro', 'Calle 10 # 5-20',  'Bruno Rios',    NULL),
    ('compra-005', 'usr-carla', '2026-10-12', 'CANCELADA',   'Calarca',    'Av Colon # 25-40', 'Carla Mejia',   NULL),
    ('compra-006', 'usr-carla', '2026-10-09', 'REEMBOLSADA', 'Calarca',    'Av Colon # 25-40', 'Carla Mejia',   '2026-10-12 11:00:00'),
    ('compra-007', 'usr-diego', '2026-10-14', 'ENTREGADA',   'Pereira',    'Cl 19 # 8-34',     'Diego Arango',  '2026-10-17 09:30:00');

INSERT INTO detalles_compra (compra_id, presentacion_id, precio_monto, precio_moneda, cantidad_valor, cantidad_unidad) VALUES
    ('compra-001', 'pres-cafe-1', 28000, 'COP', 2, 'kg'),
    ('compra-001', 'pres-taza-1', 15000, 'COP', 1, 'unidad'),
    ('compra-002', 'pres-cafe-2', 32000, 'COP', 1, 'kg'),
    ('compra-003', 'pres-cafe-1', 28000, 'COP', 3, 'kg'),
    ('compra-004', 'pres-cafe-3', 45000, 'COP', 1, 'kg'),
    ('compra-005', 'pres-cafe-2', 32000, 'COP', 2, 'kg'),
    ('compra-006', 'pres-cafe-3', 45000, 'COP', 1, 'kg'),
    ('compra-007', 'pres-cafe-2', 32000, 'COP', 1, 'kg'),
    ('compra-007', 'pres-cafe-3', 36000, 'COP', 1, 'kg');

-- compra-001: reembolso de la taza RECHAZADO. compra-006: su unica Presentacion APROBADA (por eso esta REEMBOLSADA).
-- compra-007: reembolso parcial, pres-cafe-3 APROBADO y pres-cafe-2 todavia SOLICITADO (la compra sigue ENTREGADA).
INSERT INTO solicitudes_reembolso (id, compra_id, presentacion_id, vendedor_id, motivo, fecha_solicitud, estado, fecha_resolucion) VALUES
    ('reembolso-001', 'compra-001', 'pres-taza-1', 'usr-derivados',  'La taza llego despicada',    '2026-10-11 10:00:00', 'RECHAZADO',  '2026-10-11 18:00:00'),
    ('reembolso-002', 'compra-006', 'pres-cafe-3', 'usr-tostador',   'El cafe llego humedo',       '2026-10-12 20:00:00', 'APROBADO',   '2026-10-13 09:00:00'),
    ('reembolso-003', 'compra-007', 'pres-cafe-3', 'usr-tostador',   'Bolsa rota en el envio',     '2026-10-17 12:00:00', 'APROBADO',   '2026-10-17 16:00:00'),
    ('reembolso-004', 'compra-007', 'pres-cafe-2', 'usr-caficultor', 'No era el tueste que pedi',  '2026-10-18 08:00:00', 'SOLICITADO', NULL);

-- Resenas solo sobre compras entregadas: la de Ana ya tiene respuesta del Tostador; la de Diego no.
INSERT INTO resenas (id, comprador_id, presentacion_id, vendedor_id, calificacion, texto, fecha, respuesta_texto, respuesta_fecha) VALUES
    ('resena-001', 'usr-ana',   'pres-cafe-1', 'usr-tostador',  5, 'Notas a panela y buen cuerpo',  '2026-10-11 08:00:00', 'Gracias por tu compra, Ana', '2026-10-11 12:00:00'),
    ('resena-002', 'usr-diego', 'pres-cafe-3', 'usr-tostador',  2, 'Llego con la bolsa rota',       '2026-10-17 13:00:00', NULL,                         NULL);
