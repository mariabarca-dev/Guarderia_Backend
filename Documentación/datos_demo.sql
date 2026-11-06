USE guarderia_db;

-- ---------------------------------------------------------------------
-- 1) ADMINISTRADORES
-- ---------------------------------------------------------------------
INSERT INTO usuarios (nombre, apellido, dni, direccion, telefono, nombre_usuario, clave, rol, activo)
VALUES ('Laura', 'Méndez', '40100001', 'Av. Rivadavia 4500', '1140010001', 'demo_admin1', '$2a$10$yg9TTxTipIXlFEWxH8IM3.5Xksn9oiio2zTVrzbLnUzWF0P50uqHO', 'ADMINISTRADOR', 1);
INSERT INTO administradores (usuario_id) VALUES (LAST_INSERT_ID());

INSERT INTO usuarios (nombre, apellido, dni, direccion, telefono, nombre_usuario, clave, rol, activo)
VALUES ('Diego', 'Castro', '40100002', 'Calle Corrientes 1200', '1140010002', 'demo_admin2', '$2a$10$yg9TTxTipIXlFEWxH8IM3.5Xksn9oiio2zTVrzbLnUzWF0P50uqHO', 'ADMINISTRADOR', 1);
INSERT INTO administradores (usuario_id) VALUES (LAST_INSERT_ID());

-- ---------------------------------------------------------------------
-- 2) SOCIOS (el DNI y la fecha de ingreso son obligatorios)
-- ---------------------------------------------------------------------
INSERT INTO usuarios (nombre, apellido, dni, direccion, telefono, nombre_usuario, clave, rol, activo)
VALUES ('Martín', 'Gómez', '40200001', 'Belgrano 321', '1150020001', 'demo_socio1', '$2a$10$yg9TTxTipIXlFEWxH8IM3.5Xksn9oiio2zTVrzbLnUzWF0P50uqHO', 'SOCIO', 1);
INSERT INTO socios (usuario_id, fecha_ingreso) VALUES (LAST_INSERT_ID(), '2015-03-10');

INSERT INTO usuarios (nombre, apellido, dni, direccion, telefono, nombre_usuario, clave, rol, activo)
VALUES ('Lucía', 'Fernández', '40200002', 'San Martín 874', '1150020002', 'demo_socio2', '$2a$10$yg9TTxTipIXlFEWxH8IM3.5Xksn9oiio2zTVrzbLnUzWF0P50uqHO', 'SOCIO', 1);
INSERT INTO socios (usuario_id, fecha_ingreso) VALUES (LAST_INSERT_ID(), '2018-06-22');

INSERT INTO usuarios (nombre, apellido, dni, direccion, telefono, nombre_usuario, clave, rol, activo)
VALUES ('Carlos', 'Ruiz', '40200003', 'Mitre 1560', '1150020003', 'demo_socio3', '$2a$10$yg9TTxTipIXlFEWxH8IM3.5Xksn9oiio2zTVrzbLnUzWF0P50uqHO', 'SOCIO', 1);
INSERT INTO socios (usuario_id, fecha_ingreso) VALUES (LAST_INSERT_ID(), '2020-01-15');

INSERT INTO usuarios (nombre, apellido, dni, direccion, telefono, nombre_usuario, clave, rol, activo)
VALUES ('Valeria', 'Soto', '40200004', 'Sarmiento 95', '1150020004', 'demo_socio4', '$2a$10$yg9TTxTipIXlFEWxH8IM3.5Xksn9oiio2zTVrzbLnUzWF0P50uqHO', 'SOCIO', 1);
INSERT INTO socios (usuario_id, fecha_ingreso) VALUES (LAST_INSERT_ID(), '2022-09-05');

-- ---------------------------------------------------------------------
-- 3) EMPLEADOS (código único y especialidad)
-- ---------------------------------------------------------------------
INSERT INTO usuarios (nombre, apellido, dni, direccion, telefono, nombre_usuario, clave, rol, activo)
VALUES ('Ana', 'Torres', '40300001', 'Lavalle 640', '1160030001', 'demo_empleado1', '$2a$10$yg9TTxTipIXlFEWxH8IM3.5Xksn9oiio2zTVrzbLnUzWF0P50uqHO', 'EMPLEADO', 1);
INSERT INTO empleados (usuario_id, codigo, especialidad) VALUES (LAST_INSERT_ID(), 'EMD001', 'Mecánica');

INSERT INTO usuarios (nombre, apellido, dni, direccion, telefono, nombre_usuario, clave, rol, activo)
VALUES ('Pablo', 'Ríos', '40300002', 'Alsina 2210', '1160030002', 'demo_empleado2', '$2a$10$yg9TTxTipIXlFEWxH8IM3.5Xksn9oiio2zTVrzbLnUzWF0P50uqHO', 'EMPLEADO', 1);
INSERT INTO empleados (usuario_id, codigo, especialidad) VALUES (LAST_INSERT_ID(), 'EMD002', 'Electricidad');

INSERT INTO usuarios (nombre, apellido, dni, direccion, telefono, nombre_usuario, clave, rol, activo)
VALUES ('Sofía', 'Ledesma', '40300003', 'Pueyrredón 777', '1160030003', 'demo_empleado3', '$2a$10$yg9TTxTipIXlFEWxH8IM3.5Xksn9oiio2zTVrzbLnUzWF0P50uqHO', 'EMPLEADO', 1);
INSERT INTO empleados (usuario_id, codigo, especialidad) VALUES (LAST_INSERT_ID(), 'EMD003', 'Limpieza y mantenimiento');

-- ---------------------------------------------------------------------
-- 4) ZONAS (una por tipo de vehículo; capacidad = vehículos que contiene)
-- ---------------------------------------------------------------------
INSERT INTO zonas (letra, tipo_vehiculo, capacidad_vehiculos, ancho_garage, largo_garage, activo) VALUES
('ZM', 'MOTORHOME',                3, 3.5, 10.0, 1),
('ZC', 'CASA_RODANTE_DE_ARRASTRE', 3, 3.0,  8.0, 1),
('ZR', 'CARAVANA',                 2, 2.8,  7.0, 1),
('ZT', 'TRAILER',                  2, 2.5,  6.0, 1);

-- ---------------------------------------------------------------------
-- 5) GARAGES (número, lectura de luz, servicio de mantenimiento, zona)
-- ---------------------------------------------------------------------
INSERT INTO garages (numero_garage, lectura_luz, servicio_mantenimiento, zona_id, activo) VALUES
(9101, 1250.5, 1, (SELECT id FROM zonas WHERE letra = 'ZM'), 1),
(9102,  980.0, 0, (SELECT id FROM zonas WHERE letra = 'ZM'), 1),
(9103,    0.0, 0, (SELECT id FROM zonas WHERE letra = 'ZM'), 1),
(9201,  640.8, 1, (SELECT id FROM zonas WHERE letra = 'ZC'), 1),
(9202,  412.3, 0, (SELECT id FROM zonas WHERE letra = 'ZC'), 1),
(9203,    0.0, 0, (SELECT id FROM zonas WHERE letra = 'ZC'), 1),
(9301,  305.9, 1, (SELECT id FROM zonas WHERE letra = 'ZR'), 1),
(9302,    0.0, 0, (SELECT id FROM zonas WHERE letra = 'ZR'), 1),
(9401,  150.2, 0, (SELECT id FROM zonas WHERE letra = 'ZT'), 1),
(9402,    0.0, 0, (SELECT id FROM zonas WHERE letra = 'ZT'), 1);

-- ---------------------------------------------------------------------
-- 6) VEHÍCULOS (cada uno pertenece a un socio; el empleado a cargo es opcional)
--    profundidad y ancho no superan los de los garages de su zona
-- ---------------------------------------------------------------------
INSERT INTO vehiculos (socio_id, empleado_id, nombre, matricula, tipo, profundidad, ancho, activo) VALUES
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio1'), (SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado1'), 'Don Pepe',      'DEMO-MH01', 'MOTORHOME',                9.0, 3.2, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio2'), (SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado1'), 'La Ballena',    'DEMO-MH02', 'MOTORHOME',                9.5, 3.4, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio1'), (SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado1'), 'Rodante Azul',  'DEMO-CR01', 'CASA_RODANTE_DE_ARRASTRE', 7.5, 2.8, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio3'), (SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado2'), 'El Nómade',     'DEMO-CR02', 'CASA_RODANTE_DE_ARRASTRE', 7.8, 2.9, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio3'), (SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado2'), 'Caracol',       'DEMO-CV01', 'CARAVANA',                 6.5, 2.5, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio4'), (SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado3'), 'Mini Trailer',  'DEMO-TR01', 'TRAILER',                  5.5, 2.3, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio2'), NULL,                                                              'Trailer Sur',   'DEMO-TR02', 'TRAILER',                  5.8, 2.4, 1);

-- ---------------------------------------------------------------------
-- 7) PROPIEDAD DE GARAGES (un solo socio dueño por garage; fecha de compra
--    posterior al ingreso del socio)
-- ---------------------------------------------------------------------
INSERT INTO propiedades_garage (socio_id, garage_id, fecha_compra_garage, activo) VALUES
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio1'), (SELECT id FROM garages WHERE numero_garage = 9101), '2016-05-01', 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio2'), (SELECT id FROM garages WHERE numero_garage = 9102), '2019-02-14', 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio1'), (SELECT id FROM garages WHERE numero_garage = 9201), '2017-08-30', 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio3'), (SELECT id FROM garages WHERE numero_garage = 9202), '2021-03-20', 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio3'), (SELECT id FROM garages WHERE numero_garage = 9301), '2022-01-10', 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_socio4'), (SELECT id FROM garages WHERE numero_garage = 9401), '2023-04-02', 1);

-- ---------------------------------------------------------------------
-- 8) VEHÍCULO EN GARAGE (un vehículo ocupa un garage y cada garage tiene un
--    solo vehículo; la fecha de asignación es independiente de la de compra)
-- ---------------------------------------------------------------------
INSERT INTO asignaciones_vehiculo_garage (vehiculo_id, garage_id, fecha_asignacion_garage, activo) VALUES
((SELECT id FROM vehiculos WHERE matricula = 'DEMO-MH01'), (SELECT id FROM garages WHERE numero_garage = 9101), '2016-06-15', 1),
((SELECT id FROM vehiculos WHERE matricula = 'DEMO-MH02'), (SELECT id FROM garages WHERE numero_garage = 9102), '2019-03-01', 1),
((SELECT id FROM vehiculos WHERE matricula = 'DEMO-CR01'), (SELECT id FROM garages WHERE numero_garage = 9201), '2017-09-10', 1),
((SELECT id FROM vehiculos WHERE matricula = 'DEMO-CR02'), (SELECT id FROM garages WHERE numero_garage = 9202), '2021-04-05', 1),
((SELECT id FROM vehiculos WHERE matricula = 'DEMO-CV01'), (SELECT id FROM garages WHERE numero_garage = 9301), '2022-02-20', 1),
((SELECT id FROM vehiculos WHERE matricula = 'DEMO-TR01'), (SELECT id FROM garages WHERE numero_garage = 9401), '2023-05-12', 1);

-- ---------------------------------------------------------------------
-- 9) EMPLEADOS POR ZONA (cantidad de vehículos a cargo en cada zona; la suma
--    por zona no supera la capacidad de la zona)
-- ---------------------------------------------------------------------
INSERT INTO asignaciones_empleado_zona (empleado_id, zona_id, cant_vehiculos_a_cargo, activo) VALUES
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado1'), (SELECT id FROM zonas WHERE letra = 'ZM'), 2, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado1'), (SELECT id FROM zonas WHERE letra = 'ZC'), 1, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado2'), (SELECT id FROM zonas WHERE letra = 'ZC'), 2, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado2'), (SELECT id FROM zonas WHERE letra = 'ZR'), 1, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado3'), (SELECT id FROM zonas WHERE letra = 'ZM'), 1, 1),
((SELECT id FROM usuarios WHERE nombre_usuario = 'demo_empleado3'), (SELECT id FROM zonas WHERE letra = 'ZT'), 1, 1);

-- ---------------------------------------------------------------------
-- 10) CONTROL: cantidad de filas cargadas por tabla
-- ---------------------------------------------------------------------
SELECT 'usuarios demo' AS tabla, COUNT(*) AS filas FROM usuarios WHERE nombre_usuario LIKE 'demo\_%'
UNION ALL SELECT 'zonas demo',  COUNT(*) FROM zonas WHERE letra IN ('ZM','ZC','ZR','ZT')
UNION ALL SELECT 'garages demo', COUNT(*) FROM garages WHERE numero_garage BETWEEN 9101 AND 9402
UNION ALL SELECT 'vehiculos demo', COUNT(*) FROM vehiculos WHERE matricula LIKE 'DEMO-%'
UNION ALL SELECT 'propiedades demo', COUNT(*) FROM propiedades_garage WHERE garage_id IN (SELECT id FROM garages WHERE numero_garage BETWEEN 9101 AND 9402)
UNION ALL SELECT 'vehiculo en garage demo', COUNT(*) FROM asignaciones_vehiculo_garage WHERE garage_id IN (SELECT id FROM garages WHERE numero_garage BETWEEN 9101 AND 9402)
UNION ALL SELECT 'empleado por zona demo', COUNT(*) FROM asignaciones_empleado_zona WHERE zona_id IN (SELECT id FROM zonas WHERE letra IN ('ZM','ZC','ZR','ZT'));