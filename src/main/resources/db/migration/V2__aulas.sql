-- V2: aulas y su relación con las asignaturas (convocatoria especial de la práctica 2).

CREATE TABLE aula (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(40)  NOT NULL UNIQUE,
    edificio  VARCHAR(80),
    capacidad INT NOT NULL,
    CONSTRAINT ck_aula_capacidad CHECK (capacidad > 0)
);

-- Una asignatura puede darse en varias aulas y un aula acoge varias asignaturas.
CREATE TABLE asignatura_aula (
    id_asignatura INT NOT NULL,
    id_aula       INT NOT NULL,
    PRIMARY KEY (id_asignatura, id_aula),
    CONSTRAINT fk_asignatura_aula_asignatura FOREIGN KEY (id_asignatura) REFERENCES asignatura (id) ON DELETE CASCADE,
    CONSTRAINT fk_asignatura_aula_aula FOREIGN KEY (id_aula) REFERENCES aula (id)
);
