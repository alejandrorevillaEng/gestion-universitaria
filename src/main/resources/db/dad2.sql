-- Esquema de la aplicación. Vale para MySQL 8 y para H2 en modo MySQL.
-- Es idempotente: se puede ejecutar en cada arranque sin perder datos.

CREATE TABLE IF NOT EXISTS usuario (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(50)  NOT NULL UNIQUE,
    hash_contrasena VARCHAR(255) NOT NULL,
    rol             VARCHAR(10)  NOT NULL
);

CREATE TABLE IF NOT EXISTS titulacion (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL UNIQUE,
    descripcion VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS profesor (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(120) NOT NULL,
    especialidad VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS asignatura (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    creditos      INT NOT NULL,
    capacidad     INT NOT NULL,
    id_titulacion INT NOT NULL,
    id_profesor   INT NULL,
    CONSTRAINT uq_asignatura_titulacion UNIQUE (nombre, id_titulacion),
    CONSTRAINT fk_asignatura_titulacion FOREIGN KEY (id_titulacion) REFERENCES titulacion (id),
    CONSTRAINT fk_asignatura_profesor FOREIGN KEY (id_profesor) REFERENCES profesor (id),
    CONSTRAINT ck_asignatura_capacidad CHECK (capacidad > 0),
    CONSTRAINT ck_asignatura_creditos CHECK (creditos > 0)
);

CREATE TABLE IF NOT EXISTS alumno (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(80)  NOT NULL,
    apellidos VARCHAR(120) NOT NULL,
    email     VARCHAR(160) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS matricula (
    id_alumno     INT NOT NULL,
    id_asignatura INT NOT NULL,
    fecha         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_alumno, id_asignatura),
    CONSTRAINT fk_matricula_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id) ON DELETE CASCADE,
    CONSTRAINT fk_matricula_asignatura FOREIGN KEY (id_asignatura) REFERENCES asignatura (id)
);

-- Administrador por defecto: admin / admin, como pide el enunciado.
-- La contraseña va con hash PBKDF2-SHA256 (600.000 iteraciones), nunca en claro.
-- En producción se cambia con la variable de entorno ADMIN_PASSWORD.
INSERT IGNORE INTO usuario (nombre, hash_contrasena, rol)
VALUES ('admin', 'pbkdf2_sha256$600000$2HSXpaVlQoZ+kFAHwf7Mvw==$AQXkqlpznXCnHSTtSsdp+jP4mVPB1OqFRPRw5SA4e0c=', 'ADMIN');
