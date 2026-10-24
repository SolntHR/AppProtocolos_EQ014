package com.example.appprotocolos_eq014.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class InduztekDbHelper (context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {
    companion object {
        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 1

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
                potencia_kva REAL,
                tensiones REAL,
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
                id_protocolo INTEGER PRIMARY KEY AUTOINCREMENT,
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
                id_protocolo_reemplazado INTEGER,
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
                id_protocolo INTEGER NOT NULL,
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
                id_protocolo INTEGER NOT NULL,
                id_tipo_prueba INTEGER NOT NULL,
                elemento TEXT NOT NULL,
                momento TEXT,
                realizada INTEGER NOT NULL DEFAULT 1,
                motivo_no_realizada TEXT,
                id_instrumento INTEGER,
                id_resultado INTEGER,
                comentario TEXT,
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
                valor REAL NOT NULL,
                UNIQUE(id_prueba, id_parametro),
                FOREIGN KEY (id_prueba) REFERENCES PRUEBA(id_prueba) ON DELETE CASCADE,
                FOREIGN KEY (id_parametro) REFERENCES PARAMETRO(id_parametro) ON DELETE RESTRICT
            )
        """

        // Carga de datos
        private const val SQL_PRECARGAR_ROLES = """
            INSERT INTO ROL (codigo, nombre) VALUES 
            ('TECNICO', 'Técnico de terreno'),
            ('INGENIERO', 'Ingeniero de pruebas'),
            ('JEFE_TECNICO', 'Jefe técnico'),
            ('CLIENTE', 'Usuario cliente')
        """

        private const val SQL_PRECARGAR_TIPOS_EQUIPO = """
            INSERT INTO TIPO_EQUIPO (nombre, clasificacion) VALUES 
            ('Transformador', 'PRIMARIO'),
            ('Interruptor', 'PRIMARIO'),
            ('Celda MT', 'PRIMARIO'),
            ('Cable de poder', 'PRIMARIO'),
            ('Relé de protección', 'SECUNDARIO')
        """

        private const val SQL_PRECARGAR_ESTADOS = """
            INSERT INTO ESTADO_PROTOCOLO (codigo) VALUES 
            ('BORRADOR'),
            ('REGISTRADO_TERRENO'),
            ('OBSERVADO'),
            ('FORMALIZADO'),
            ('ANULADO')
        """

        private const val SQL_PRECARGAR_RESULTADOS = """
            INSERT INTO RESULTADO (codigo) VALUES 
            ('APROBADO'),
            ('OBSERVADO'),
            ('RECHAZADO')
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
