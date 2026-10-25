package com.example.tiendaeyg;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductosDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 7; // Subimos a versión 7
    public static final String DATABASE_NAME = "Tienda.db";

    public static final String TABLE_RECETAS = "recetas";
    public static final String COL_ID_COMBO = "id_combo";
    public static final String COL_ID_INGREDIENTE = "id_ingrediente";

    public static final String TABLE_HISTORIAL = "ventas_historial";
    public static final String COL_HIST_NOMBRE = "nombre";
    public static final String COL_HIST_PRECIO = "precio";
    public static final String COL_HIST_PAGO = "metodo_pago";
    public static final String COL_HIST_FECHA = "fecha";

    public ProductosDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla de productos con opciones_venta y imagen_uri
        String SQL_CREATE_PRODUCTOS =
                "CREATE TABLE " + ProductosContract.ProductoEntry.TABLE_NAME + " (" +
                ProductosContract.ProductoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ProductosContract.ProductoEntry.COLUMN_NOMBRE + " TEXT NOT NULL," +
                ProductosContract.ProductoEntry.COLUMN_PRECIO + " REAL NOT NULL," +
                ProductosContract.ProductoEntry.COLUMN_STOCK + " INTEGER NOT NULL DEFAULT 0," +
                "imagen_uri TEXT," +
                "opciones_venta TEXT)";
        db.execSQL(SQL_CREATE_PRODUCTOS);

        String SQL_CREATE_RECETAS =
                "CREATE TABLE " + TABLE_RECETAS + " (" +
                COL_ID_COMBO + " INTEGER NOT NULL," +
                COL_ID_INGREDIENTE + " INTEGER NOT NULL)";
        db.execSQL(SQL_CREATE_RECETAS);

        String SQL_CREATE_HISTORIAL =
                "CREATE TABLE " + TABLE_HISTORIAL + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_HIST_NOMBRE + " TEXT NOT NULL," +
                COL_HIST_PRECIO + " REAL NOT NULL," +
                COL_HIST_PAGO + " TEXT NOT NULL," +
                COL_HIST_FECHA + " TEXT NOT NULL)";
        db.execSQL(SQL_CREATE_HISTORIAL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProductosContract.ProductoEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECETAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORIAL);
        onCreate(db);
    }
}