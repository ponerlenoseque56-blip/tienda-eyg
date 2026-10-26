package com.example.tiendaeyg;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Pantalla simple para saber que la app abrió
        TextView tv = new TextView(this);
        tv.setText("Probando conexión a Firebase...");
        tv.setTextSize(24);
        setContentView(tv);

        // LA PRUEBA DE FUEGO
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tienda-eyg-1312-default-rtdb.firebaseio.com/");
        DatabaseReference ref = database.getReference("inventario");
        ref.child("prueba_conexion").setValue("¡Funciona!")
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(MainActivity.this, "¡Conexión exitosa!", Toast.LENGTH_LONG).show();
                tv.setText("¡CONECTADO! Revisá Firebase.");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                tv.setText("ERROR: " + e.getMessage());
            });
    }
}
