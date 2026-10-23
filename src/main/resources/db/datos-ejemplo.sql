-- Datos de ejemplo (inventados) para probar la aplicación.
-- Solo se cargan si la base de datos no tiene titulaciones.

INSERT INTO titulacion (nombre, descripcion) VALUES
    ('Grado en Ingeniería Informática', 'Cuatro cursos, 240 ECTS'),
    ('Grado en Ingeniería Telemática', 'Redes, servicios y comunicaciones'),
    ('Grado en Matemáticas', 'Álgebra, análisis y estadística');

INSERT INTO profesor (nombre, especialidad) VALUES
    ('Laura Martín', 'Programación'),
    ('Sergio Ortega', 'Redes'),
    ('Elena Castillo', 'Matemática aplicada'),
    ('Pablo Navas', 'Sistemas distribuidos');

INSERT INTO asignatura (nombre, creditos, capacidad, id_titulacion, id_profesor)
SELECT 'Programación I', 6, 40, t.id, p.id FROM titulacion t, profesor p
WHERE t.nombre = 'Grado en Ingeniería Informática' AND p.nombre = 'Laura Martín';

INSERT INTO asignatura (nombre, creditos, capacidad, id_titulacion, id_profesor)
SELECT 'Aplicaciones Distribuidas', 6, 3, t.id, p.id FROM titulacion t, profesor p
WHERE t.nombre = 'Grado en Ingeniería Informática' AND p.nombre = 'Pablo Navas';

INSERT INTO asignatura (nombre, creditos, capacidad, id_titulacion, id_profesor)
SELECT 'Redes de Computadores', 6, 30, t.id, p.id FROM titulacion t, profesor p
WHERE t.nombre = 'Grado en Ingeniería Telemática' AND p.nombre = 'Sergio Ortega';

INSERT INTO asignatura (nombre, creditos, capacidad, id_titulacion, id_profesor)
SELECT 'Cálculo', 9, 50, t.id, p.id FROM titulacion t, profesor p
WHERE t.nombre = 'Grado en Matemáticas' AND p.nombre = 'Elena Castillo';

INSERT INTO asignatura (nombre, creditos, capacidad, id_titulacion, id_profesor)
SELECT 'Estadística', 6, 45, t.id, NULL FROM titulacion t
WHERE t.nombre = 'Grado en Matemáticas';

INSERT INTO aula (nombre, edificio, capacidad) VALUES
    ('A-101', 'Aulario A', 60),
    ('A-204', 'Aulario A', 35),
    ('L-03', 'Laboratorios', 24);

INSERT INTO asignatura_aula (id_asignatura, id_aula)
SELECT a.id, au.id FROM asignatura a, aula au
WHERE a.nombre = 'Programación I' AND au.nombre = 'A-101';

INSERT INTO asignatura_aula (id_asignatura, id_aula)
SELECT a.id, au.id FROM asignatura a, aula au
WHERE a.nombre = 'Aplicaciones Distribuidas' AND au.nombre IN ('A-204', 'L-03');

INSERT INTO alumno (nombre, apellidos, email) VALUES
    ('Marta', 'Gil Romero', 'marta.gil@ejemplo.es'),
    ('Daniel', 'Soto Prieto', 'daniel.soto@ejemplo.es'),
    ('Lucía', 'Herrera Vidal', 'lucia.herrera@ejemplo.es'),
    ('Andrés', 'Molina Cano', 'andres.molina@ejemplo.es');

INSERT INTO matricula (id_alumno, id_asignatura)
SELECT al.id, a.id FROM alumno al, asignatura a
WHERE a.nombre = 'Aplicaciones Distribuidas' AND al.email IN ('marta.gil@ejemplo.es', 'daniel.soto@ejemplo.es');

INSERT INTO matricula (id_alumno, id_asignatura)
SELECT al.id, a.id FROM alumno al, asignatura a
WHERE a.nombre = 'Programación I' AND al.email IN ('marta.gil@ejemplo.es', 'lucia.herrera@ejemplo.es', 'andres.molina@ejemplo.es');
