package com.example.tiendaeyg;

import android.provider.BaseColumns;

public final class ProductosContract {
    // Evita que alguien cree instancias de esta clase
    private ProductosContract() {}

    public static class ProductoEntry implements BaseColumns {
        public static final String TABLE_NAME = "productos";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_PRECIO = "precio";
        public static final String COLUMN_STOCK = "stock";
    }
}