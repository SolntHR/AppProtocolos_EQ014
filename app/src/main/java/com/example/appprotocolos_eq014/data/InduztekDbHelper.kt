package com.example.appprotocolos_eq014.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class InduztekDbHelper (context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {
    companion object {
        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 7

        private const val SQL_CREAR_ROL = """
            CREATE TABLE ROL (
                id_rol INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT UNIQUE NOT NULL,
                nombre TEXT NOT NULL
            )
        """

        private const val SQL_CREAR_PERMISO = """
            CREATE TABLE PERMISO (
                id_permiso INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT UNIQUE NOT NULL,
                descripcion TEXT NOT NULL
            )
        """

        private const val SQL_CREAR_REGION = """
            CREATE TABLE REGION (
                id_region INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT UNIQUE NOT NULL
            )
        """

        private const val SQL_CREAR_TIPO_EQUIPO = """
            CREATE TABLE TIPO_EQUIPO (
                id_tipo_equipo INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT UNIQUE NOT NULL,
                clasificacion TEXT 
            )
        """

        private const val SQL_CREAR_TIPO_PRUEBA = """
            CREATE TABLE TIPO_PRUEBA (
                id_tipo_prueba INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT UNIQUE NOT NULL,
                nombre TEXT NOT NULL
            )
        """

        private const val SQL_CREAR_ESTADO_PROTOCOLO = """
            CREATE TABLE ESTADO_PROTOCOLO (
                id_estado INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT UNIQUE NOT NULL
            )
        """

        private const val SQL_CREAR_RESULTADO = """
            CREATE TABLE RESULTADO (
                id_resultado INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT UNIQUE NOT NULL
            )
        """

        private const val SQL_CREAR_INSTRUMENTO = """
            CREATE TABLE INSTRUMENTO (
                id_instrumento INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_tipo TEXT NOT NULL,
                marca TEXT,
                modelo TEXT,
                numero_serie TEXT
            )
        """

        private const val SQL_CREAR_USUARIO = """
            CREATE TABLE USUARIO (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                id_rol INTEGER NOT NULL,
                rut TEXT UNIQUE NOT NULL,
                nombre TEXT NOT NULL,
                correo TEXT UNIQUE NOT NULL,
                telefono TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1,
                giro TEXT,
                contacto_nombre TEXT,
                contacto_cargo TEXT,
                pregunta_seguridad TEXT,
                respuesta_seguridad TEXT,
                foto_perfil TEXT,
                FOREIGN KEY (id_rol) REFERENCES ROL(id_rol) ON DELETE RESTRICT
            )
        """

        private const val SQL_CREAR_ROL_PERMISO = """
            CREATE TABLE ROL_PERMISO (
                id_rol INTEGER NOT NULL,
                id_permiso INTEGER NOT NULL,
                PRIMARY KEY (id_rol, id_permiso),
                FOREIGN KEY (id_rol) REFERENCES ROL(id_rol) ON DELETE CASCADE,
                FOREIGN KEY (id_permiso) REFERENCES PERMISO(id_permiso) ON DELETE CASCADE
            )
        """

        private const val SQL_CREAR_COMUNA = """
            CREATE TABLE COMUNA (
                id_comuna INTEGER PRIMARY KEY AUTOINCREMENT,
                id_region INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                FOREIGN KEY (id_region) REFERENCES REGION(id_region) ON DELETE RESTRICT
            )
        """

        private const val SQL_CREAR_PARAMETRO = """
            CREATE TABLE PARAMETRO (
                id_parametro INTEGER PRIMARY KEY AUTOINCREMENT,
                id_tipo_prueba INTEGER NOT NULL,
                codigo TEXT NOT NULL,
                nombre TEXT NOT NULL,
                unidad TEXT NOT NULL,
                FOREIGN KEY (id_tipo_prueba) REFERENCES TIPO_PRUEBA(id_tipo_prueba) ON DELETE CASCADE
            )
        """

        private const val SQL_CREAR_TIPO_EQUIPO_PRUEBA = """
            CREATE TABLE TIPO_EQUIPO_PRUEBA (
                id_tipo_equipo INTEGER NOT NULL,
                id_tipo_prueba INTEGER NOT NULL,
                PRIMARY KEY (id_tipo_equipo, id_tipo_prueba),
                FOREIGN KEY (id_tipo_equipo) REFERENCES TIPO_EQUIPO(id_tipo_equipo) ON DELETE CASCADE,
                FOREIGN KEY (id_tipo_prueba) REFERENCES TIPO_PRUEBA(id_tipo_prueba) ON DELETE CASCADE
            )
        """

        private const val SQL_CREAR_CALIBRACION = """
            CREATE TABLE CALIBRACION (
                id_calibracion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_instrumento INTEGER NOT NULL,
                fecha_calibracion TEXT NOT NULL,
                fecha_vencimiento TEXT NOT NULL,
                FOREIGN KEY (id_instrumento) REFERENCES INSTRUMENTO(id_instrumento) ON DELETE CASCADE
            )
        """

        private const val SQL_CREAR_INSTALACION = """
            CREATE TABLE INSTALACION (
                id_instalacion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_usuario_cliente INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                ubicacion TEXT,
                id_comuna INTEGER NOT NULL,
                observaciones_acceso TEXT,
                FOREIGN KEY (id_usuario_cliente) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT,
                FOREIGN KEY (id_comuna) REFERENCES COMUNA(id_comuna) ON DELETE RESTRICT
            )
        """

        private const val SQL_CREAR_EQUIPO = """
            CREATE TABLE EQUIPO (
                id_equipo INTEGER PRIMARY KEY AUTOINCREMENT,
                id_instalacion INTEGER NOT NULL,
                tag TEXT NOT NULL,
                id_tipo_equipo INTEGER NOT NULL,
                marca TEXT,
                modelo TEXT,
                numero_serie TEXT,
                anio INTEGER,
                observaciones TEXT,
                potencia_kva REAL,
                tensiones TEXT,
                corriente REAL,
                max_contactos_uohm REAL,
                UNIQUE(id_instalacion, tag),
                FOREIGN KEY (id_instalacion) REFERENCES INSTALACION(id_instalacion) ON DELETE CASCADE,
                FOREIGN KEY (id_tipo_equipo) REFERENCES TIPO_EQUIPO(id_tipo_equipo) ON DELETE RESTRICT
            )
        """

        private const val SQL_CREAR_VISITA = """
            CREATE TABLE VISITA (
                id_visita INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_ot TEXT UNIQUE NOT NULL,
                id_instalacion INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                tipo_visita TEXT NOT NULL,
                id_ingeniero_cargo INTEGER NOT NULL,
                fecha_hora_cierre TEXT,
                FOREIGN KEY (id_instalacion) REFERENCES INSTALACION(id_instalacion) ON DELETE RESTRICT,
                FOREIGN KEY (id_ingeniero_cargo) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT
            )
        """

        private const val SQL_CREAR_VISITA_PARTICIPANTE = """
            CREATE TABLE VISITA_PARTICIPANTE (
                id_visita INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                PRIMARY KEY (id_visita, id_usuario),
                FOREIGN KEY (id_visita) REFERENCES VISITA(id_visita) ON DELETE CASCADE,
                FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT
            )
        """

        private const val SQL_CREAR_RECEPCION_ACTA = """
            CREATE TABLE RECEPCION_ACTA (
                id_visita INTEGER PRIMARY KEY,
                receptor_nombre TEXT NOT NULL,
                receptor_rut TEXT NOT NULL,
                receptor_cargo TEXT NOT NULL,
                fecha_hora_recepcion TEXT NOT NULL,
                FOREIGN KEY (id_visita) REFERENCES VISITA(id_visita) ON DELETE CASCADE
            )
        """

        private const val SQL_CREAR_PROTOCOLO = """
            CREATE TABLE PROTOCOLO (
                id_protocolo TEXT PRIMARY KEY NOT NULL,
                numero TEXT UNIQUE NOT NULL,
                id_visita INTEGER NOT NULL,
                id_equipo INTEGER NOT NULL,
                id_usuario_registra INTEGER NOT NULL,
                fecha_hora_prueba TEXT NOT NULL,
                temperatura_c REAL,
                humedad_pct REAL,
                descripcion_mantenimiento TEXT,
                observaciones TEXT,
                id_estado INTEGER NOT NULL,
                id_protocolo_reemplazado TEXT,
                UNIQUE(id_visita, id_equipo),
                FOREIGN KEY (id_visita) REFERENCES VISITA(id_visita) ON DELETE CASCADE,
                FOREIGN KEY (id_equipo) REFERENCES EQUIPO(id_equipo) ON DELETE RESTRICT,
                FOREIGN KEY (id_usuario_registra) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT,
                FOREIGN KEY (id_estado) REFERENCES ESTADO_PROTOCOLO(id_estado) ON DELETE RESTRICT,
                FOREIGN KEY (id_protocolo_reemplazado) REFERENCES PROTOCOLO(id_protocolo) ON DELETE SET NULL
            )
        """

        private const val SQL_CREAR_HISTORIAL_ESTADO = """
            CREATE TABLE HISTORIAL_ESTADO (
                id_historial INTEGER PRIMARY KEY AUTOINCREMENT,
                id_protocolo TEXT NOT NULL,
                id_estado_anterior INTEGER,
                id_estado_nuevo INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                fecha_hora TEXT NOT NULL,
                motivo TEXT,
                FOREIGN KEY (id_protocolo) REFERENCES PROTOCOLO(id_protocolo) ON DELETE CASCADE,
                FOREIGN KEY (id_estado_anterior) REFERENCES ESTADO_PROTOCOLO(id_estado) ON DELETE RESTRICT,
                FOREIGN KEY (id_estado_nuevo) REFERENCES ESTADO_PROTOCOLO(id_estado) ON DELETE RESTRICT,
                FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT
            )
        """

        private const val SQL_CREAR_PRUEBA = """
            CREATE TABLE PRUEBA (
                id_prueba INTEGER PRIMARY KEY AUTOINCREMENT,
                id_protocolo TEXT NOT NULL,
                id_tipo_prueba INTEGER NOT NULL,
                elemento TEXT NOT NULL,
                momento TEXT,
                realizada INTEGER NOT NULL DEFAULT 1,
                motivo_no_realizada TEXT,
                id_instrumento INTEGER,
                id_resultado INTEGER,
                comentario TEXT,
                ingresado_manualmente INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (id_protocolo) REFERENCES PROTOCOLO(id_protocolo) ON DELETE CASCADE,
                FOREIGN KEY (id_tipo_prueba) REFERENCES TIPO_PRUEBA(id_tipo_prueba) ON DELETE RESTRICT,
                FOREIGN KEY (id_instrumento) REFERENCES INSTRUMENTO(id_instrumento) ON DELETE RESTRICT,
                FOREIGN KEY (id_resultado) REFERENCES RESULTADO(id_resultado) ON DELETE RESTRICT
            )
        """

        private const val SQL_CREAR_MEDICION = """
            CREATE TABLE MEDICION (
                id_medicion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_prueba INTEGER NOT NULL,
                id_parametro INTEGER NOT NULL,
                elemento TEXT,
                valor REAL NOT NULL,
                unidad TEXT NOT NULL,            
                orden INTEGER DEFAULT 0,
                UNIQUE(id_prueba, id_parametro),
                FOREIGN KEY (id_prueba) REFERENCES PRUEBA(id_prueba) ON DELETE CASCADE,
                FOREIGN KEY (id_parametro) REFERENCES PARAMETRO(id_parametro) ON DELETE RESTRICT
            )
        """

        private const val SQL_PRECARGAR_ROLES = """
            INSERT INTO ROL (id_rol, codigo, nombre) VALUES 
            (1, 'TECNICO', 'Técnico de terreno'),
            (2, 'INGENIERO', 'Ingeniero de pruebas'),
            (3, 'JEFE_TECNICO', 'Jefe técnico'),
            (4, 'CLIENTE', 'Usuario cliente');
        """

        private const val SQL_PRECARGAR_ESTADOS = """
            INSERT INTO ESTADO_PROTOCOLO (id_estado, codigo) VALUES 
            (1, 'BORRADOR'), (2, 'REGISTRADO_TERRENO'), (3, 'OBSERVADO'), (4, 'FORMALIZADO'), (5, 'ANULADO');
        """

        private const val SQL_PRECARGAR_RESULTADOS = """
            INSERT INTO RESULTADO (id_resultado, codigo) VALUES 
            (1, 'APROBADO'), (2, 'OBSERVADO'), (3, 'RECHAZADO');
        """

        private const val SQL_PRECARGAR_TIPOS_EQUIPO = """
            INSERT INTO TIPO_EQUIPO (id_tipo_equipo, nombre, clasificacion) VALUES 
            (1, 'Transformador', 'PRIMARIO'),
            (2, 'Interruptor', 'PRIMARIO'),
            (3, 'Celda MT', 'PRIMARIO'),
            (4, 'Cable de poder', 'PRIMARIO'),
            (5, 'Relé de protección', 'SECUNDARIO');
        """

        private const val SQL_PRECARGAR_REGIONES = """
            INSERT INTO REGION (id_region, nombre) VALUES (13, 'Región Metropolitana');
        """

        private const val SQL_PRECARGAR_COMUNAS = """
            INSERT INTO COMUNA (id_comuna, id_region, nombre) VALUES 
            (1, 13, 'Lampa'), (2, 13, 'San Bernardo'), (3, 13, 'Til Til'), 
            (4, 13, 'La Florida'), (5, 13, 'Puente Alto'), (6, 13, 'Huechuraba');
        """
        private const val SQL_PRECARGAR_INSTRUMENTOS = """
            INSERT INTO INSTRUMENTO (id_instrumento, nombre_tipo, marca, modelo, numero_serie) VALUES
            (1, 'Megóhmetro 5 kV', 'Megger', 'MIT525', 'MIT-0045821'),
            (2, 'Megóhmetro 10 kV', 'Megger', 'MIT1025', 'MIT-0071233'),
            (3, 'Medidor de relación (TTR)', 'Megger', 'TTR300', 'TTR-30044'),
            (4, 'Micro-ohmímetro', 'Megger', 'DLRO10X', 'DLR-10882'),
            (5, 'Equipo de inyección secundaria', 'Omicron', 'CMC 356', 'CMC-HB411');
        """

        private const val SQL_PRECARGAR_CALIBRACIONES = """
            INSERT INTO CALIBRACION (id_calibracion, id_instrumento, fecha_calibracion, fecha_vencimiento) VALUES
            (1, 1, '2026-03-15', '2027-03-15'),
            (2, 2, '2025-10-10', '2026-10-10'),
            (3, 3, '2026-06-02', '2027-06-02'),
            (4, 4, '2025-08-20', '2026-08-20'),
            (5, 5, '2026-01-05', '2027-01-05');
        """

        private const val SQL_PRECARGAR_TIPOS_PRUEBA = """
            INSERT INTO TIPO_PRUEBA (id_tipo_prueba, codigo, nombre) VALUES
            (1, 'AISLACION', 'Resistencia de aislación'),
            (2, 'TTR', 'Relación de transformación'),
            (3, 'DEVANADOS', 'Resistencia de devanados'),
            (4, 'CONTACTOS', 'Resistencia de contactos'),
            (5, 'OPERACION_RELE', 'Prueba de operación (inyección secundaria)');
        """

        private const val SQL_PRECARGAR_TIPO_EQUIPO_PRUEBA = """
            INSERT INTO TIPO_EQUIPO_PRUEBA (id_tipo_equipo, id_tipo_prueba) VALUES
            (1, 1), (1, 2), (1, 3),
            (2, 1), (2, 4),
            (3, 1), (3, 4),
            (4, 1),
            (5, 5);
        """

        private const val SQL_PRECARGAR_PARAMETROS = """
            INSERT INTO PARAMETRO (id_parametro, id_tipo_prueba, codigo, nombre, unidad) VALUES
            (1, 1, 'LECTURA_1MIN', 'Lectura a 1 min', 'MΩ'),
            (2, 1, 'LECTURA_10MIN', 'Lectura a 10 min', 'MΩ'),
            (3, 1, 'TENSION_PRUEBA', 'Tensión de prueba', 'kV'),
            (4, 2, 'RELACION_TEORICA', 'Relación teórica', '-'),
            (5, 2, 'RELACION_MEDIDA', 'Relación medida', '-'),
            (6, 2, 'POSICION_TAP', 'Posición del cambiador de derivaciones', '-'),
            (7, 3, 'RESISTENCIA_FASE', 'Resistencia por fase', 'mΩ'),
            (8, 4, 'RESISTENCIA_POLO', 'Resistencia por polo', 'µΩ'),
            (9, 5, 'CORRIENTE_AJUSTE', 'Ajuste de corriente de operación', 'A'),
            (10, 5, 'CORRIENTE_MEDIDA', 'Corriente medida de operación', 'A'),
            (11, 5, 'TIEMPO_ESPERADO', 'Tiempo esperado', 'ms'),
            (12, 5, 'TIEMPO_MEDIDO', 'Tiempo medido', 'ms');
        """

        private const val SQL_PRECARGAR_USUARIOS = """
            INSERT INTO USUARIO (id_usuario, id_rol, rut, nombre, correo, telefono, password_hash, activo, giro, contacto_nombre, contacto_cargo, pregunta_seguridad, respuesta_seguridad) VALUES
            (1, 1, '17.284.519-3', 'Matías Godoy Herrera', 'mgodoy@induztek.ejemplo', '+56 9 8123 4567', '123456', 1, NULL, NULL, NULL, '¿Cuál es el nombre de tu primera mascota?', 'firulais'),
            (2, 1, '19.412.087-7', 'Javiera Tapia Muñoz', 'jtapia@induztek.ejemplo', '+56 9 6674 9021', '123456', 1, NULL, NULL, NULL, '¿En qué ciudad nacieron tus padres?', 'santiago'),
            (3, 1, '26.104.583-4', 'Carlos Rondón Pérez', 'crdon@induztek.ejemplo', '+56 9 5521 7743', '123456', 1, NULL, NULL, NULL, '¿Cuál es tu comida favorita?', 'pizza'),
            (4, 1, '20.137.648-3', 'Diego Valenzuela Ríos', 'dvalenzuela@induztek.ejemplo', '+56 9 7390 1856', '123456', 1, NULL, NULL, NULL, '¿Cuál fue el modelo de tu primer auto?', 'toyota'),
            (5, 2, '15.906.334-8', 'Andrea Contreras Silva', 'acontreras@induztek.ejemplo', '+56 9 7456 2210', '123456', 1, NULL, NULL, NULL, '¿Cuál es el nombre de tu mejor amigo de la infancia?', 'pedro'),
            (6, 2, '16.553.270-8', 'Sebastián Morales Gutiérrez', 'smorales@induztek.ejemplo', '+56 9 8812 4430', '123456', 1, NULL, NULL, NULL, '¿Cuál es el nombre de tu primera mascota?', 'rocky'),
            (7, 3, '13.871.905-7', 'Ricardo Espinoza Lagos', 'respinoza@induztek.ejemplo', '+56 9 9345 1187', '123456', 1, NULL, NULL, NULL, '¿En qué ciudad nacieron tus padres?', 'valparaiso'),
            
            (8, 4, '76.482.915-8', 'Envasadora del Pacífico S.A.', 'mfuentes@envpacifico.ejemplo', '+56 9 6612 3480', '123456', 1, 'Agroindustria', 'Marcela Fuentes Rojas', 'Jefa de Mantenimiento', '¿Cuál es tu comida favorita?', 'sushi'),
            (9, 4, '96.731.540-0', 'Sanitaria Valle Central S.A.', 'jarancibia@svcentral.ejemplo', '+56 9 4471 2093', '123456', 1, 'Servicios sanitarios', 'Jorge Arancibia Leiva', 'Jefe de Planta', '¿Cuál fue el modelo de tu primer auto?', 'nissan'),
            (10, 4, '77.105.362-9', 'Minera Cerro Alto SpA', 'pnavarro@cerroalto.ejemplo', '+56 9 3308 6615', '123456', 1, 'Minería', 'Patricio Navarro Soto', 'Jefe de Mantenimiento Eléctrico', '¿Cuál es el nombre de tu mejor amigo de la infancia?', 'juan'),
            (11, 4, '76.903.218-5', 'Clínica Los Aromos Ltda.', 'cmendez@losaromos.ejemplo', '+56 9 5218 6604', '123456', 1, 'Salud', 'Carolina Méndez Pizarro', 'Supervisora', '¿Cuál es el nombre de tu primera mascota?', 'luna');
        """

        private const val SQL_PRECARGAR_OPERATIVO = """
            INSERT INTO INSTALACION (id_instalacion, id_usuario_cliente, nombre, ubicacion, id_comuna, observaciones_acceso) VALUES
            (1, 8, 'Planta Lampa', 'Sector industrial', 1, 'Inducción obligatoria de 30 min'),
            (2, 9, 'Planta Elevadora Sur', 'Sector rural poniente', 2, 'Sala eléctrica subterránea, sin señal'),
            (3, 10, 'Faena Til Til', 'Acceso por Ruta 5 Norte', 3, 'Sin señal en toda la faena'),
            (4, 11, 'Edificio Clínico', 'Zona urbana', 4, 'Cortes solo domingos 06:00-10:00');
        """
        private const val SQL_PRECARGAR_EQUIPO = """
            INSERT INTO EQUIPO (id_equipo, id_instalacion, tag, id_tipo_equipo, marca, modelo, numero_serie, potencia_kva, tensiones, corriente, max_contactos_uohm) VALUES
            (1, 1, 'TR-01', 1, 'Rhona', 'RH-18733', 'RH-18733', 1500, 12.0, 72.0, NULL),
            (2, 1, 'TR-02', 1, 'Rhona', 'RH-21904', 'RH-21904', 1000, 12.0, 48.0, NULL),
            (3, 2, 'TR-A', 1, 'Tusan', 'TS-5521', 'TS-5521', 2500, 23.0, 62.0, NULL),
            (4, 4, 'CEL-MT-03', 3, 'Ormazabal', 'OR-77812', 'OR-77812', NULL, 12.0, 630.0, 60.0),
            (5, 3, 'INT-52-1', 2, 'Schneider', 'SE-449021', 'SE-449021', NULL, 23.0, 1250.0, 45.0);
        """
        private const val SQL_PRECARGAR_VISITA = """
            INSERT INTO VISITA (id_visita, numero_ot, id_instalacion, fecha, tipo_visita, id_ingeniero_cargo, fecha_hora_cierre) VALUES
            (1, 'OT-2026-1187', 1, '2026-09-26', 'SOLO_PRUEBAS', 5, NULL),
            (2, 'OT-2026-1154', 4, '2026-09-20', 'PRUEBAS_Y_MANT', 6, '2026-09-20 09:55'),
            (3, 'OT-2024-0951', 1, '2024-09-14', 'SOLO_PRUEBAS', 5, '2024-09-14 12:10'),
            (4, 'OT-2025-1032', 1, '2025-09-13', 'SOLO_PRUEBAS', 5, '2025-09-13 11:45'),
            (5, 'OT-2026-1102', 3, '2026-09-08', 'SOLO_PRUEBAS', 6, '2026-09-08 13:20');
        """
        private const val SQL_PRECARGAR_PROTOCOLO = """
            INSERT INTO PROTOCOLO (id_protocolo, numero, id_visita, id_equipo, id_usuario_registra, fecha_hora_prueba, temperatura_c, humedad_pct, descripcion_mantenimiento, observaciones, id_estado, id_protocolo_reemplazado) VALUES
            ('PT-2024-0312', 'PT-2024-0312', 3, 1, 1, '2024-09-14 10:00:00', 18.0, 55.0, NULL, 'Prueba de rutina', 4, NULL),
            ('PT-2025-0288', 'PT-2025-0288', 4, 1, 2, '2025-09-13 09:30:00', 21.0, 48.0, NULL, 'Prueba anual', 4, NULL),
            ('PT-2026-0401', 'PT-2026-0401', 1, 1, 1, '2026-09-26 09:40:00', 19.0, 60.0, 'Limpieza de aisladores BT', 'IP bajo en AT-T, revisar', 2, NULL),
            ('PT-2026-0402', 'PT-2026-0402', 1, 2, 1, '2026-09-26 11:15:00', 19.0, 60.0, NULL, NULL, 2, NULL),
            ('PT-2026-0389', 'PT-2026-0389', 2, 4, 3, '2026-09-20 11:00:00', 22.0, 45.0, NULL, 'Falta n° de serie instrumento', 3, NULL),
            ('PT-2026-0360', 'PT-2026-0360', 5, 5, 2, '2026-09-08 10:30:00', 27.0, 20.0, NULL, NULL, 4, NULL);
        """
        private const val SQL_PRECARGAR_PARTICIPANTES = """
            INSERT INTO VISITA_PARTICIPANTE (id_visita, id_usuario) VALUES
            (1, 1), (2, 3), (3, 1), (4, 2), (5, 2);
        """

        private const val SQL_PRECARGAR_RECEPCIONES = """
            INSERT INTO RECEPCION_ACTA (id_visita, receptor_nombre, receptor_rut, receptor_cargo, fecha_hora_recepcion) VALUES
            (2, 'Tomás Leiva Araya', '18.390.452-3', 'Supervisor Eléctrico', '2026-09-20 09:55'),
            (3, 'Marcela Fuentes Rojas', '14.226.791-8', 'Jefa de Mantenimiento', '2024-09-14 12:10'),
            (4, 'Marcela Fuentes Rojas', '14.226.791-8', 'Jefa de Mantenimiento', '2025-09-13 11:45'),
            (5, 'Patricio Navarro Soto', '13.349.876-1', 'Superintendente de Mantenimiento', '2026-09-08 13:20');
        """

        private const val SQL_PRECARGAR_PRUEBAS = """
            INSERT INTO PRUEBA (id_prueba, id_protocolo, id_tipo_prueba, elemento, momento, realizada, motivo_no_realizada, id_instrumento, id_resultado, comentario) VALUES
            (1, 'PT-2024-0312', 1, 'AT-T', 'UNICA', 1, NULL, 1, 1, NULL),
            (2, 'PT-2025-0288', 1, 'AT-T', 'UNICA', 1, NULL, 1, 1, NULL),
            (3, 'PT-2026-0401', 1, 'AT-T', 'UNICA', 1, NULL, 1, 2, 'IP bajo en AT-T, revisar'),
            (4, 'PT-2026-0401', 2, 'Fase H1-X1', 'UNICA', 0, 'Cliente no autorizó desenergizar el lado BT', NULL, NULL, NULL),
            (5, 'PT-2026-0402', 1, 'AT-T', 'UNICA', 1, NULL, 1, 1, NULL),
            (6, 'PT-2026-0402', 2, 'Fase H1-X1', 'UNICA', 1, NULL, 3, 1, NULL),
            (7, 'PT-2026-0389', 1, 'Polo 1', 'UNICA', 1, NULL, 1, 1, NULL),
            (8, 'PT-2026-0389', 4, 'Polo 1', 'UNICA', 1, NULL, NULL, 2, 'Falta n° de serie del instrumento'),
            (9, 'PT-2026-0360', 4, 'Polo 1', 'UNICA', 1, NULL, NULL, 3, NULL);
        """

        private const val SQL_PRECARGAR_MEDICIONES = """
            INSERT INTO MEDICION (id_medicion, id_prueba, id_parametro, elemento, valor, unidad, orden) VALUES
            (1, 1, 1, 'AT-T', 12500, 'MΩ', 1), (2, 1, 2, 'AT-T', 31250, 'MΩ', 2), (3, 1, 3, 'AT-T', 5, 'kV', 3),
            (4, 2, 1, 'AT-T', 9800, 'MΩ', 1), (5, 2, 2, 'AT-T', 21560, 'MΩ', 2), (6, 2, 3, 'AT-T', 5, 'kV', 3),
            (7, 3, 1, 'AT-T', 6100, 'MΩ', 1), (8, 3, 2, 'AT-T', 10980, 'MΩ', 2), (9, 3, 3, 'AT-T', 5, 'kV', 3),
            (10, 5, 1, 'AT-T', 15200, 'MΩ', 1), (11, 5, 2, 'AT-T', 36480, 'MΩ', 2), (12, 5, 3, 'AT-T', 5, 'kV', 3),
            (13, 6, 4, 'Fase H1-X1', 30.0, '-', 1), (14, 6, 5, 'Fase H1-X1', 30.09, '-', 2), (15, 6, 6, 'Fase H1-X1', 3, '-', 3),
            (16, 7, 1, 'Polo 1', 8500, 'MΩ', 1), (17, 7, 3, 'Polo 1', 5, 'kV', 2),
            (18, 8, 8, 'Polo 1', 65, 'µΩ', 1),
            (19, 9, 8, 'Polo 1', 56, 'µΩ', 1);
        """
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROL)
        db.execSQL(SQL_CREAR_PERMISO)
        db.execSQL(SQL_CREAR_REGION)
        db.execSQL(SQL_CREAR_TIPO_EQUIPO)
        db.execSQL(SQL_CREAR_TIPO_PRUEBA)
        db.execSQL(SQL_CREAR_ESTADO_PROTOCOLO)
        db.execSQL(SQL_CREAR_RESULTADO)
        db.execSQL(SQL_CREAR_INSTRUMENTO)

        db.execSQL(SQL_CREAR_USUARIO)
        db.execSQL(SQL_CREAR_ROL_PERMISO)
        db.execSQL(SQL_CREAR_COMUNA)
        db.execSQL(SQL_CREAR_PARAMETRO)
        db.execSQL(SQL_CREAR_TIPO_EQUIPO_PRUEBA)
        db.execSQL(SQL_CREAR_CALIBRACION)

        db.execSQL(SQL_CREAR_INSTALACION)
        db.execSQL(SQL_CREAR_EQUIPO)
        db.execSQL(SQL_CREAR_VISITA)
        db.execSQL(SQL_CREAR_VISITA_PARTICIPANTE)
        db.execSQL(SQL_CREAR_RECEPCION_ACTA)
        db.execSQL(SQL_CREAR_PROTOCOLO)
        db.execSQL(SQL_CREAR_HISTORIAL_ESTADO)
        db.execSQL(SQL_CREAR_PRUEBA)
        db.execSQL(SQL_CREAR_MEDICION)

        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_ESTADOS)
        db.execSQL(SQL_PRECARGAR_RESULTADOS)
        db.execSQL(SQL_PRECARGAR_TIPOS_EQUIPO)
        db.execSQL(SQL_PRECARGAR_REGIONES)
        db.execSQL(SQL_PRECARGAR_COMUNAS)
        db.execSQL(SQL_PRECARGAR_INSTRUMENTOS)
        db.execSQL(SQL_PRECARGAR_CALIBRACIONES)
        db.execSQL(SQL_PRECARGAR_TIPOS_PRUEBA)
        db.execSQL(SQL_PRECARGAR_TIPO_EQUIPO_PRUEBA)
        db.execSQL(SQL_PRECARGAR_PARAMETROS)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_OPERATIVO)
        db.execSQL(SQL_PRECARGAR_EQUIPO)
        db.execSQL(SQL_PRECARGAR_VISITA)
        db.execSQL(SQL_PRECARGAR_PROTOCOLO)
        db.execSQL(SQL_PRECARGAR_PARTICIPANTES)
        db.execSQL(SQL_PRECARGAR_RECEPCIONES)
        db.execSQL(SQL_PRECARGAR_PRUEBAS)
        db.execSQL(SQL_PRECARGAR_MEDICIONES)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS MEDICION")
        db.execSQL("DROP TABLE IF EXISTS PRUEBA")
        db.execSQL("DROP TABLE IF EXISTS HISTORIAL_ESTADO")
        db.execSQL("DROP TABLE IF EXISTS PROTOCOLO")
        db.execSQL("DROP TABLE IF EXISTS RECEPCION_ACTA")
        db.execSQL("DROP TABLE IF EXISTS VISITA_PARTICIPANTE")
        db.execSQL("DROP TABLE IF EXISTS VISITA")
        db.execSQL("DROP TABLE IF EXISTS EQUIPO")
        db.execSQL("DROP TABLE IF EXISTS INSTALACION")

        db.execSQL("DROP TABLE IF EXISTS CALIBRACION")
        db.execSQL("DROP TABLE IF EXISTS TIPO_EQUIPO_PRUEBA")
        db.execSQL("DROP TABLE IF EXISTS PARAMETRO")
        db.execSQL("DROP TABLE IF EXISTS COMUNA")
        db.execSQL("DROP TABLE IF EXISTS ROL_PERMISO")
        db.execSQL("DROP TABLE IF EXISTS USUARIO")

        db.execSQL("DROP TABLE IF EXISTS INSTRUMENTO")
        db.execSQL("DROP TABLE IF EXISTS RESULTADO")
        db.execSQL("DROP TABLE IF EXISTS ESTADO_PROTOCOLO")
        db.execSQL("DROP TABLE IF EXISTS TIPO_PRUEBA")
        db.execSQL("DROP TABLE IF EXISTS TIPO_EQUIPO")
        db.execSQL("DROP TABLE IF EXISTS REGION")
        db.execSQL("DROP TABLE IF EXISTS PERMISO")
        db.execSQL("DROP TABLE IF EXISTS ROL")

        onCreate(db)
    }

}
