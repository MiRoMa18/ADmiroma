-- ==========================================
-- CREAR BASE DE DATOS
-- ==========================================
CREATE DATABASE IF NOT EXISTS control_horario;
USE control_horario;

-- ==========================================
-- TABLA TRABAJADOR
-- ==========================================
CREATE TABLE trabajador (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numero_tarjeta VARCHAR(20) UNIQUE NOT NULL,
    pin VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100),
    email VARCHAR(100),
    rol ENUM('ADMIN', 'TRABAJADOR') DEFAULT 'TRABAJADOR',
    fecha_alta DATE
);

-- ==========================================
-- TABLA FICHAJE
-- ==========================================
CREATE TABLE fichaje (
    id INT PRIMARY KEY AUTO_INCREMENT,
    trabajador_id INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    tipo ENUM('ENTRADA', 'SALIDA') NOT NULL,
    clima VARCHAR(50),
    notas TEXT,
    FOREIGN KEY (trabajador_id) REFERENCES trabajador(id) ON DELETE CASCADE
);

-- ==========================================
-- INSERT TRABAJADORES
-- ==========================================
INSERT INTO trabajador (numero_tarjeta, pin, nombre, apellidos, email, rol, fecha_alta) VALUES
('1001', '1234', 'Juan', 'García Pérez', 'juan.garcia@empresa.com', 'ADMIN', '2023-01-10'),
('1002', '5678', 'María', 'López Martín', 'maria.lopez@empresa.com', 'TRABAJADOR', '2023-02-15'),
('1003', '9012', 'Carlos', 'Rodríguez Sanz', 'carlos.rodriguez@empresa.com', 'TRABAJADOR', '2023-03-20'),
('1004', '3456', 'Ana', 'Fernández Gil', 'ana.fernandez@empresa.com', 'TRABAJADOR', '2023-04-05'),
('1005', '7890', 'Pedro', 'Martínez Luna', 'pedro.martinez@empresa.com', 'TRABAJADOR', '2023-05-12'),
('1006', '2468', 'Laura', 'Sánchez Ruiz', 'laura.sanchez@empresa.com', 'ADMIN', '2023-01-15'),
('1007', '1357', 'David', 'Gómez Torres', 'david.gomez@empresa.com', 'TRABAJADOR', '2023-06-18'),
('1008', '9753', 'Elena', 'Díaz Moreno', 'elena.diaz@empresa.com', 'TRABAJADOR', '2023-07-22');

-- ==========================================
-- INSERT FICHAJES - Semana del 15 al 19 Enero 2024
-- ==========================================

-- LUNES 15/01/2024 - SOLEADO --
-- Juan (id=1)
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(1, '2024-01-15 08:00:00', 'ENTRADA', 'Soleado', NULL),
(1, '2024-01-15 14:00:00', 'SALIDA', 'Soleado', 'Pausa comida'),
(1, '2024-01-15 15:00:00', 'ENTRADA', 'Soleado', 'Vuelta comida'),
(1, '2024-01-15 18:30:00', 'SALIDA', 'Soleado', NULL);

-- María (id=2)
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(2, '2024-01-15 08:15:00', 'ENTRADA', 'Soleado', NULL),
(2, '2024-01-15 15:15:00', 'SALIDA', 'Soleado', NULL);

-- Carlos (id=3)
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(3, '2024-01-15 09:00:00', 'ENTRADA', 'Soleado', NULL),
(3, '2024-01-15 13:30:00', 'SALIDA', 'Soleado', 'Comida'),
(3, '2024-01-15 14:30:00', 'ENTRADA', 'Soleado', NULL),
(3, '2024-01-15 18:00:00', 'SALIDA', 'Soleado', NULL);

-- Ana (id=4)
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(4, '2024-01-15 09:45:00', 'ENTRADA', 'Soleado', 'Retraso - tráfico'),
(4, '2024-01-15 14:00:00', 'SALIDA', 'Soleado', 'Comida'),
(4, '2024-01-15 15:00:00', 'ENTRADA', 'Soleado', NULL),
(4, '2024-01-15 19:00:00', 'SALIDA', 'Soleado', 'Recuperó horas');

-- MARTES 16/01/2024 - LLUVIA --
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(1, '2024-01-16 08:05:00', 'ENTRADA', 'Lluvia', NULL),
(1, '2024-01-16 14:00:00', 'SALIDA', 'Lluvia', 'Comida'),
(1, '2024-01-16 15:00:00', 'ENTRADA', 'Lluvia', NULL),
(1, '2024-01-16 18:00:00', 'SALIDA', 'Lluvia', NULL),

(2, '2024-01-16 08:00:00', 'ENTRADA', 'Lluvia', NULL),
(2, '2024-01-16 15:00:00', 'SALIDA', 'Lluvia', NULL),

(3, '2024-01-16 08:30:00', 'ENTRADA', 'Lluvia', NULL),
(3, '2024-01-16 13:30:00', 'SALIDA', 'Lluvia', 'Comida'),
(3, '2024-01-16 14:30:00', 'ENTRADA', 'Lluvia', NULL),
(3, '2024-01-16 18:30:00', 'SALIDA', 'Lluvia', NULL),

(4, '2024-01-16 08:15:00', 'ENTRADA', 'Lluvia', NULL),
(4, '2024-01-16 17:00:00', 'SALIDA', 'Lluvia', NULL),

(5, '2024-01-16 07:45:00', 'ENTRADA', 'Lluvia', NULL),
(5, '2024-01-16 13:00:00', 'SALIDA', 'Lluvia', 'Comida'),
(5, '2024-01-16 14:00:00', 'ENTRADA', 'Lluvia', NULL),
(5, '2024-01-16 17:30:00', 'SALIDA', 'Lluvia', NULL);

-- MIÉRCOLES 17/01/2024 - NUBLADO --
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(1, '2024-01-17 08:10:00', 'ENTRADA', 'Nublado', NULL),
(1, '2024-01-17 14:00:00', 'SALIDA', 'Nublado', 'Comida'),
(1, '2024-01-17 15:00:00', 'ENTRADA', 'Nublado', NULL),
(1, '2024-01-17 19:00:00', 'SALIDA', 'Nublado', 'Horas extra'),

(2, '2024-01-17 08:20:00', 'ENTRADA', 'Nublado', NULL),
(2, '2024-01-17 15:20:00', 'SALIDA', 'Nublado', NULL),

(3, '2024-01-17 09:00:00', 'ENTRADA', 'Nublado', NULL),
(3, '2024-01-17 13:00:00', 'SALIDA', 'Nublado', 'Comida'),
(3, '2024-01-17 14:00:00', 'ENTRADA', 'Nublado', NULL),
(3, '2024-01-17 18:00:00', 'SALIDA', 'Nublado', NULL),

(5, '2024-01-17 08:00:00', 'ENTRADA', 'Nublado', NULL),
(5, '2024-01-17 16:30:00', 'SALIDA', 'Nublado', NULL),

(6, '2024-01-17 07:30:00', 'ENTRADA', 'Nublado', NULL),
(6, '2024-01-17 13:30:00', 'SALIDA', 'Nublado', 'Comida'),
(6, '2024-01-17 14:30:00', 'ENTRADA', 'Nublado', NULL),
(6, '2024-01-17 18:00:00', 'SALIDA', 'Nublado', NULL);

-- JUEVES 18/01/2024 - TORMENTA --
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(1, '2024-01-18 08:00:00', 'ENTRADA', 'Tormenta', NULL),
(1, '2024-01-18 14:00:00', 'SALIDA', 'Tormenta', 'Comida'),
(1, '2024-01-18 15:00:00', 'ENTRADA', 'Tormenta', NULL),
(1, '2024-01-18 18:15:00', 'SALIDA', 'Tormenta', NULL),

(2, '2024-01-18 08:10:00', 'ENTRADA', 'Tormenta', NULL),
(2, '2024-01-18 15:10:00', 'SALIDA', 'Tormenta', NULL),

(3, '2024-01-18 09:00:00', 'ENTRADA', 'Tormenta', NULL),
(3, '2024-01-18 13:30:00', 'SALIDA', 'Tormenta', 'Comida'),
(3, '2024-01-18 14:30:00', 'ENTRADA', 'Tormenta', NULL),
(3, '2024-01-18 18:00:00', 'SALIDA', 'Tormenta', NULL),

(4, '2024-01-18 08:30:00', 'ENTRADA', 'Tormenta', NULL),
(4, '2024-01-18 14:00:00', 'SALIDA', 'Tormenta', 'Comida'),
(4, '2024-01-18 15:00:00', 'ENTRADA', 'Tormenta', NULL),
(4, '2024-01-18 17:30:00', 'SALIDA', 'Tormenta', NULL),

(7, '2024-01-18 08:00:00', 'ENTRADA', 'Tormenta', NULL),
(7, '2024-01-18 16:00:00', 'SALIDA', 'Tormenta', NULL);

-- VIERNES 19/01/2024 - PARCIALMENTE NUBLADO --
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(1, '2024-01-19 08:00:00', 'ENTRADA', 'Parcialmente nublado', NULL),
(1, '2024-01-19 14:00:00', 'SALIDA', 'Parcialmente nublado', 'Comida'),
(1, '2024-01-19 15:00:00', 'ENTRADA', 'Parcialmente nublado', NULL),
(1, '2024-01-19 17:00:00', 'SALIDA', 'Parcialmente nublado', 'Viernes tarde libre'),

(2, '2024-01-19 08:00:00', 'ENTRADA', 'Parcialmente nublado', NULL),
(2, '2024-01-19 14:00:00', 'SALIDA', 'Parcialmente nublado', 'Viernes intensivo'),

(3, '2024-01-19 09:00:00', 'ENTRADA', 'Parcialmente nublado', NULL),
(3, '2024-01-19 13:00:00', 'SALIDA', 'Parcialmente nublado', 'Comida'),
(3, '2024-01-19 14:00:00', 'ENTRADA', 'Parcialmente nublado', NULL),
(3, '2024-01-19 17:00:00', 'SALIDA', 'Parcialmente nublado', NULL),

(4, '2024-01-19 08:15:00', 'ENTRADA', 'Parcialmente nublado', NULL),
(4, '2024-01-19 14:15:00', 'SALIDA', 'Parcialmente nublado', NULL),

(5, '2024-01-19 08:00:00', 'ENTRADA', 'Parcialmente nublado', NULL),
(5, '2024-01-19 15:00:00', 'SALIDA', 'Parcialmente nublado', NULL),

(6, '2024-01-19 07:45:00', 'ENTRADA', 'Parcialmente nublado', NULL),
(6, '2024-01-19 13:00:00', 'SALIDA', 'Parcialmente nublado', 'Comida'),
(6, '2024-01-19 14:00:00', 'ENTRADA', 'Parcialmente nublado', NULL),
(6, '2024-01-19 16:30:00', 'SALIDA', 'Parcialmente nublado', NULL);

-- ==========================================
-- CASOS ESPECIALES CON DIFERENTES CLIMAS
-- ==========================================

-- Caso 1: Olvidó fichar salida (día nevado)
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(8, '2024-01-15 08:00:00', 'ENTRADA', 'Nieve', NULL);

-- Caso 2: Media jornada (mañana despejada)
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(7, '2024-01-15 08:00:00', 'ENTRADA', 'Despejado', NULL),
(7, '2024-01-15 12:00:00', 'SALIDA', 'Despejado', 'Media jornada');

-- Caso 3: Turno de noche (noche despejada)
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(8, '2024-01-16 22:00:00', 'ENTRADA', 'Despejado', 'Turno noche'),
(8, '2024-01-17 06:00:00', 'SALIDA', 'Niebla', 'Fin turno noche');

-- Caso 4: Salida temprana (día ventoso)
INSERT INTO fichaje (trabajador_id, fecha_hora, tipo, clima, notas) VALUES
(7, '2024-01-16 08:00:00', 'ENTRADA', 'Ventoso', NULL),
(7, '2024-01-16 12:30:00', 'SALIDA', 'Ventoso', 'Cita médica');

-- ==========================================
-- VERIFICACIÓN
-- ==========================================
--SELECT 'Total Trabajadores' as tabla, COUNT(*) as total FROM trabajador
--UNION ALL
--SELECT 'Total Fichajes' as tabla, COUNT(*) as total FROM fichaje;

-- Ver variedad de climas usados
--SELECT DISTINCT clima FROM fichaje ORDER BY clima;