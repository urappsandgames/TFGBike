package com.tfg.carlos.tfgbike.BD;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Carlos on 16/03/2015.
 *
 * Crea la base de datos local en el dispositivo si no existe.
 */
public class BaseDeDatosLocal extends SQLiteOpenHelper{
    public static String DB_PATH = "/data/databases/";
    public static String DB_NAME = "db_TFGBike";
    private final Context myContext;
    public static int v_db = 1;

    //Sentencias SQL para Crear y actualizar
    String sqlCreate1 = "CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "_id INTEGER, email TEXT, contrasena TEXT, nombre TEXT, apellido TEXT, sexo TEXT, edad INTEGER, peso INTEGER, needUpdate BOOLEAN DEFAULT 0, UNIQUE (_id))";
    String sqlCreate2 = "CREATE TABLE bicis (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "_id INTEGER, marca TEXT, modelo TEXT, tipo TEXT, peso DOUBLE, km DOUBLE, precio DOUBLE, gasto DOUBLE, usuario TEXT, predeterminada BOOLEAN, needUpdate BOOLEAN DEFAULT 0, UNIQUE (_id))";
    String sqlCreate3 = "CREATE TABLE componentes (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "_id INTEGER, marca TEXT, modelo TEXT, tipo TEXT, peso DOUBLE, fecha DATE, estado TEXT, usuario TEXT, idbici INTEGER, precio DOUBLE, gasto DOUBLE, notas TEXT, needUpdate BOOLEAN DEFAULT 0, nuevo BOOLEAN DEFAULT 0, UNIQUE (_id))";
    String sqlCreate4 = "CREATE TABLE videos (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " tipocomponente TEXT, Instalar TEXT, Eliminar TEXT, Mantenimiento TEXT)";
    String sqlCreate5 = "CREATE TABLE actividades (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "_id INTEGER, usuario TEXT, idbici TEXT, date DATE, datosRuta TEXT, needUpdate BOOLEAN DEFAULT 0, nuevo BOOLEAN DEFAULT 0, UNIQUE (_id))";
    //String sqlUpdate = "ALTER TABLE usuarios ADD COLUMN peso INTEGER;";

    //constructor
    public BaseDeDatosLocal(Context contexto, String nombre, SQLiteDatabase.CursorFactory factory, int version){
        super(contexto, nombre, factory, version);
        this.myContext = contexto;
        //Log.i("info", "BD CONSTRUCTOR");
    }

    //Crear la BD, este metodo solo se ejecutará una vez, aqui deberán crearse todas las tablas e inicializarlas si es necesario
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("info", "BD ONCREATE");
        if( db.isReadOnly()){
            db = getWritableDatabase();
        }
        db.execSQL(sqlCreate1);
        Log.i("BDLOCAL", "creada la tabla usuarios");
        db.execSQL(sqlCreate2);
        Log.i("BDLOCAL", "creada la tabla bicis");
        db.execSQL(sqlCreate3);
        Log.i("BDLOCAL", "creada la tabla componentes");
        db.execSQL(sqlCreate4);
        Log.i("BDLOCAL", "creada la tabla videos");
        db.execSQL(sqlCreate5);
        Log.i("BDLOCAL", "creada la tabla actividades");
    }

    //Para crear más campos en nuestra BD
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.i("info", "BD ONUPGRADE");
        //if(newVersion > oldVersion){
            //db.execSQL(sqlUpdate);
        //}
    }
}
