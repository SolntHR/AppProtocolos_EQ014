package com.example.appprotocolos_eq014.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class InduztekDbHelper (context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {
    companion object {
        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 3

        // Tablas padre
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

        // Tablas hijas

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

        // Tablas operativas

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

        // Carga de datos
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
            (4, 'Micro-ohmímetro', 'Megger', 'DLRO10X', 'DLR-108822'),
            (5, 'Equipo de inyección secundaria', 'Omicron', 'CMC 356', 'CMC-HB411');
        """

        // Usuarios (Personal y Clientes)
        private const val SQL_PRECARGAR_USUARIOS = """
            INSERT INTO USUARIO (id_usuario, id_rol, rut, nombre, correo, telefono, password_hash, activo, giro, contacto_nombre, contacto_cargo, pregunta_seguridad, respuesta_seguridad) VALUES
            -- Personal Induztek (Rol 1=Técnico, 2=Ingeniero, 3=Jefe)
            (1, 1, '17.284.519-3', 'Matías Godoy Herrera', 'mgodoy@induztek.ejemplo', '+56900000101', '123456', 1, NULL, NULL, NULL, '¿Cuál es el nombre de tu primera mascota?', 'firulais'),
            (2, 1, '19.412.087-7', 'Javiera Tapia Muñoz', 'jtapia@induztek.ejemplo', '+56900000102', '123456', 1, NULL, NULL, NULL, '¿En qué ciudad nacieron tus padres?', 'santiago'),
            (3, 1, '26.104.583-4', 'Carlos Rondón Pérez', 'crdon@induztek.ejemplo', '+56900000103', '123456', 1, NULL, NULL, NULL, '¿Cuál es tu comida favorita?', 'pizza'),
            (4, 1, '20.137.648-3', 'Diego Valenzuela Ríos', 'dvalenzuela@induztek.ejemplo', '+56900000104', '123456', 1, NULL, NULL, NULL, '¿Cuál fue el modelo de tu primer auto?', 'toyota'),
            (5, 2, '15.906.334-8', 'Andrea Contreras Silva', 'acontreras@induztek.ejemplo', '+56900000105', '123456', 1, NULL, NULL, NULL, '¿Cuál es el nombre de tu mejor amigo de la infancia?', 'pedro'),
            (6, 2, '16.553.270-8', 'Sebastián Morales Gutiérrez', 'smorales@induztek.ejemplo', '+56900000106', '123456', 1, NULL, NULL, NULL, '¿Cuál es el nombre de tu primera mascota?', 'rocky'),
            (7, 3, '13.871.905-7', 'Ricardo Espinoza Lagos', 'respinoza@induztek.ejemplo', '+56900000107', '123456', 1, NULL, NULL, NULL, '¿En qué ciudad nacieron tus padres?', 'valparaiso'),
            
            -- Usuarios Clientes (Rol 4=Cliente)
            (8, 4, '76.482.915-8', 'Envasadora del Pacífico S.A.', 'mfuentes@envpacifico.ejemplo', '+56900000108', '123456', 1, 'Agroindustria', 'Marcela Fuentes Rojas', 'Jefa de Mantenimiento', '¿Cuál es tu comida favorita?', 'sushi'),
            (9, 4, '96.731.540-0', 'Sanitaria Valle Central S.A.', 'jarancibia@svcentral.ejemplo', '+56900000109', '123456', 1, 'Servicios sanitarios', 'Jorge Arancibia Leiva', 'Jefe de Planta', '¿Cuál fue el modelo de tu primer auto?', 'nissan'),
            (10, 4, '77.105.362-9', 'Minera Cerro Alto SpA', 'pnavarro@cerroalto.ejemplo', '+56900000110', '123456', 1, 'Minería', 'Patricio Navarro Soto', 'Jefe de Mantenimiento Eléctrico', '¿Cuál es el nombre de tu mejor amigo de la infancia?', 'juan'),
            (11, 4, '76.903.218-5', 'Clínica Los Aromos Ltda.', 'cmendez@losaromos.ejemplo', '+56900000111', '123456', 1, 'Salud', 'Carolina Méndez Pizarro', 'Supervisora', '¿Cuál es el nombre de tu primera mascota?', 'luna');
        """

        // Instalaciones, Equipos, Visitas y Protocolos históricos
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
            INSERT INTO VISITA (id_visita, numero_ot, id_instalacion, fecha, tipo_visita, id_ingeniero_cargo) VALUES
            (1, 'OT-2026-1187', 1, '2026-09-26', 'SOLO_PRUEBAS', 5),
            (2, 'OT-2026-1154', 4, '2026-09-20', 'PRUEBAS_Y_MANT', 6);
        """
        private const val SQL_PRECARGAR_PROTOCOLO = """
            INSERT INTO PROTOCOLO (id_protocolo, numero, id_visita, id_equipo, id_usuario_registra, fecha_hora_prueba, temperatura_c, humedad_pct, descripcion_mantenimiento, observaciones, id_estado, id_protocolo_reemplazado) VALUES
            ('PT-2024-0312', 'PT-2024-0312', 1, 1, 1, '2024-09-14 10:00:00', 18.0, 55.0, NULL, 'Prueba de rutina', 4, NULL),
            ('PT-2025-0288', 'PT-2025-0288', 1, 1, 2, '2025-09-13 09:30:00', 21.0, 48.0, NULL, 'Prueba anual', 4, NULL),
            ('PT-2026-0401', 'PT-2026-0401', 1, 1, 1, '2026-09-26 09:40:00', 19.0, 60.0, 'Limpieza de aisladores BT', 'IP bajo en AT-T, revisar', 2, NULL),
            ('PT-2026-0389', 'PT-2026-0389', 2, 4, 3, '2026-09-20 11:00:00', 22.0, 45.0, NULL, 'Falta n° de serie instrumento', 3, NULL);
        """
    }

    // Habilitar llaves foraneas
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Creamos las tablas de padre a hijos
    override fun onCreate(db: SQLiteDatabase) {
        // padre
        db.execSQL(SQL_CREAR_ROL)
        db.execSQL(SQL_CREAR_PERMISO)
        db.execSQL(SQL_CREAR_REGION)
        db.execSQL(SQL_CREAR_TIPO_EQUIPO)
        db.execSQL(SQL_CREAR_TIPO_PRUEBA)
        db.execSQL(SQL_CREAR_ESTADO_PROTOCOLO)
        db.execSQL(SQL_CREAR_RESULTADO)
        db.execSQL(SQL_CREAR_INSTRUMENTO)

        // hijos
        db.execSQL(SQL_CREAR_USUARIO)
        db.execSQL(SQL_CREAR_ROL_PERMISO)
        db.execSQL(SQL_CREAR_COMUNA)
        db.execSQL(SQL_CREAR_PARAMETRO)
        db.execSQL(SQL_CREAR_TIPO_EQUIPO_PRUEBA)
        db.execSQL(SQL_CREAR_CALIBRACION)

        // operativas
        db.execSQL(SQL_CREAR_INSTALACION)
        db.execSQL(SQL_CREAR_EQUIPO)
        db.execSQL(SQL_CREAR_VISITA)
        db.execSQL(SQL_CREAR_VISITA_PARTICIPANTE)
        db.execSQL(SQL_CREAR_RECEPCION_ACTA)
        db.execSQL(SQL_CREAR_PROTOCOLO)
        db.execSQL(SQL_CREAR_HISTORIAL_ESTADO)
        db.execSQL(SQL_CREAR_PRUEBA)
        db.execSQL(SQL_CREAR_MEDICION)

        // carga de datos en tablas
        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_ESTADOS)
        db.execSQL(SQL_PRECARGAR_RESULTADOS)
        db.execSQL(SQL_PRECARGAR_TIPOS_EQUIPO)
        db.execSQL(SQL_PRECARGAR_REGIONES)
        db.execSQL(SQL_PRECARGAR_COMUNAS)
        db.execSQL(SQL_PRECARGAR_INSTRUMENTOS)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_OPERATIVO)

    }

    // Drop
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // De tablas hijas a tabla padre
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
