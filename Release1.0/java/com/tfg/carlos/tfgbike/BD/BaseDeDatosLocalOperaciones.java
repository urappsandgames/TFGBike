package com.tfg.carlos.tfgbike.BD;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Carlos on 06/05/2015.
 * Ejecuta diferentes operaciones que utiliza la aplicación sobre la base de datos local
 */
public class BaseDeDatosLocalOperaciones {

    private final Context myContext;

    public BaseDeDatosLocalOperaciones(Context contexto){
        this.myContext = contexto;
    }

    //inserta los values en la tabla dada en la base local
    public void insertBDLocal(ContentValues values, String tabla){
        //Log.d("Info", "BD Insert CERRADA");
        //Preparamos la BD local
        BaseDeDatosLocal db1 = new BaseDeDatosLocal(myContext, BaseDeDatosLocal.DB_NAME, null, BaseDeDatosLocal.v_db);
        SQLiteDatabase db = db1.getWritableDatabase();

        //-1 if an error occurred
        if(db.insert(tabla, null, values) == -1){
            Log.d("ERROR", "BaseDeDatosLocalOperaciones: WriteBDLocal");
        }

        //cerrar databases
        db.close();
        db1.close();
        //Log.d("Info", "BD BD CERRADA");
    }


    //Devuelve los values generados al ejecutar select para la query dada
    public ArrayList<ContentValues> selectBDLocal(String selectQuery){
        //Log.d("Info", "BD Select");
        ArrayList<ContentValues> result = new ArrayList<>();

        //Preparamos la BD local
        BaseDeDatosLocal db1 = new BaseDeDatosLocal(myContext, BaseDeDatosLocal.DB_NAME, null, BaseDeDatosLocal.v_db);
        SQLiteDatabase db = db1.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);
        int j=0; //indice de registros

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Log.d("Info", "BD select, column count: "+c.getColumnCount());
            //Recorremos el cursor hasta que no haya más contenido en el registro
            do {
                //añadir contentvalues para el registro
                result.add(new ContentValues());
                //recorremos las columnas del registro y las empaquetamos a un ContentValues
                for(int i=0; i<c.getColumnCount(); i++) {
                    String columna = c.getColumnName(i);
                    String valor = c.getString(i);
                    result.get(j).put(columna, valor);
                    //Log.d("Info", columna + " :" + valor);
                }
                j++;
            } while(c.moveToNext());
        }
        else{
            Log.d("Info", "BD Select no devolvio ningun registro");
        }

        //cerrar databases
        c.close();
        db.close();
        db1.close();
        //Log.d("Info", "BD CERRADA");
        return result;
    }

    //sobrescribe la tabla dada con los valores
    public boolean replaceBDLocal(ContentValues values, String tabla){
        //Log.d("Info", "BD Replace");

        //Preparamos la BD local
        BaseDeDatosLocal db1 = new BaseDeDatosLocal(myContext, BaseDeDatosLocal.DB_NAME, null, BaseDeDatosLocal.v_db);
        SQLiteDatabase db = db1.getWritableDatabase();
       //-1 if an error occurred
        if (db.replace(tabla, null, values) == -1) {
            Log.d("Info", "BD ERROR en replace");
            db.close();
            db1.close();
            return false;
        }
        else{
            db.close();
            db1.close();
            return true;
        }
    }

    public boolean updateBDLocal(String table, ContentValues values, String whereClause, String[] whereArgs){
        //Log.d("Info", "BD update");
        BaseDeDatosLocal db1 = new BaseDeDatosLocal(myContext, BaseDeDatosLocal.DB_NAME, null, BaseDeDatosLocal.v_db);
        SQLiteDatabase db = db1.getReadableDatabase();
        db.update(table, values, whereClause, whereArgs);
        db.close();
        db1.close();
        //Log.d("Info", "BD BD CERRADA");
        return true;
    }


    //ejecuta la query dada
    public boolean queryBDLocal(String query){
        //Log.d("Info", "BD Query");
        BaseDeDatosLocal db1 = new BaseDeDatosLocal(myContext, BaseDeDatosLocal.DB_NAME, null, BaseDeDatosLocal.v_db);
        SQLiteDatabase db = db1.getReadableDatabase();
        db.execSQL(query);
        db.close();
        db1.close();
        //Log.d("Info", "BD BD CERRADA");
        return true;
    }

}
