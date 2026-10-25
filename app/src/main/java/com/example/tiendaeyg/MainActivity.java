package com.example.tiendaeyg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    Button btnTabVentas, btnTabInventario, btnTabGanancias, btnAgregarFlotante;
    LinearLayout pantallaVentas, pantallaGanancias;
    View pantallaInventario;
    
    GridView gridVentas, gridInventario;
    
    EditText editBuscar, editBuscarInventario;
    TextView txtCajaEfectivo, txtCajaTransf;
    
    // Listas de Ventas
    ArrayList<String> listaNombres = new ArrayList<>();
    ArrayList<Integer> listaIds = new ArrayList<>();
    ArrayList<Integer> listaStocks = new ArrayList<>();
    ArrayList<Double> listaPrecios = new ArrayList<>();
    ArrayList<String> listaOpciones = new ArrayList<>();
    ArrayList<String> listaImagenes = new ArrayList<>();
    ArrayAdapter<String> adaptadorVentas;
    
    // Listas de Inventario
    ArrayList<String> invNombres = new ArrayList<>();
    ArrayList<Integer> invIds = new ArrayList<>();
    ArrayList<Integer> invStocks = new ArrayList<>();
    ArrayList<String> invImagenes = new ArrayList<>();
    ArrayAdapter<String> adaptadorInventario;
    
    public static double cajaEfectivo = 0.0, cajaTransferencia = 0.0;

    TextView txtGananciaEfectivo, txtGananciaTransf;
    ListView listaHistorial;
    ArrayList<String> historialItems = new ArrayList<>();
    ArrayAdapter<String> adaptadorHistorial;

    ProductosDbHelper dbHelper;
    ArrayList<String> todosLosNombres = new ArrayList<>();
    ArrayList<Integer> todosLosIds = new ArrayList<>();
    ArrayList<Integer> todosLosStocks = new ArrayList<>();
    ArrayList<Double> todosLosPrecios = new ArrayList<>();
    ArrayList<String> todosLosOpciones = new ArrayList<>();
    ArrayList<String> todosLosImagenes = new ArrayList<>();
    
    private LinearLayout contenedorOpciones;
    private ArrayList<Spinner> listaCantidades = new ArrayList<>();
    private ArrayList<EditText> listaPreciosOpc = new ArrayList<>();

    private String rutaImagenSeleccionada = "";
    private ImageView vistaPreviaImagen;
    private int idProductoEditando = -1; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new ProductosDbHelper(this);

        btnTabVentas = findViewById(R.id.btnTabVentas);
        btnTabInventario = findViewById(R.id.btnTabInventario);
        btnTabGanancias = findViewById(R.id.btnTabGanancias);
        pantallaVentas = findViewById(R.id.pantallaVentas);
        pantallaInventario = findViewById(R.id.pantallaInventario);
        pantallaGanancias = findViewById(R.id.pantallaGanancias);

        btnTabVentas.setOnClickListener(v -> mostrarPestana(1));
        btnTabInventario.setOnClickListener(v -> mostrarPestana(2));
        btnTabGanancias.setOnClickListener(v -> mostrarPestana(3));

        // Ventas
        editBuscar = findViewById(R.id.editBuscar);
        gridVentas = findViewById(R.id.gridVentas);
        txtCajaEfectivo = findViewById(R.id.txtCajaEfectivo);
        txtCajaTransf = findViewById(R.id.txtCajaTransf);
        
        adaptadorVentas = crearAdaptadorGrid(listaNombres, listaImagenes, listaPrecios);
        gridVentas.setAdapter(adaptadorVentas);
        gridVentas.setOnItemClickListener((parent, view, position, id) -> mostrarDialogoVenta(position));
        
        editBuscar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { filtrarListaVentas(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });

        // Inventario
        editBuscarInventario = findViewById(R.id.editBuscarInventario);
        gridInventario = findViewById(R.id.gridInventario);
        btnAgregarFlotante = findViewById(R.id.btnAgregarFlotante);
        
        adaptadorInventario = crearAdaptadorGrid(invNombres, invImagenes, null);
        gridInventario.setAdapter(adaptadorInventario);

        editBuscarInventario.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { filtrarListaInventario(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });

        btnAgregarFlotante.setOnClickListener(v -> mostrarDialogoProducto(-1)); 
        
        // Clic normal para editar
        gridInventario.setOnItemClickListener((parent, view, position, id) -> {
            int idProd = invIds.get(position);
            mostrarDialogoProducto(idProd); 
        });

        // Clic largo para borrar
        gridInventario.setOnItemLongClickListener((parent, view, position, id) -> {
            mostrarDialogoBorrar(position);
            return true;
        });

        // Ganancias
        txtGananciaEfectivo = findViewById(R.id.txtGananciaEfectivo);
        txtGananciaTransf = findViewById(R.id.txtGananciaTransf);
        listaHistorial = findViewById(R.id.listaHistorial);
        adaptadorHistorial = crearAdaptadorHistorial(historialItems);
        listaHistorial.setAdapter(adaptadorHistorial);

        mostrarPestana(1);
    }

    private void mostrarPestana(int numero) {
        pantallaVentas.setVisibility(View.GONE);
        pantallaInventario.setVisibility(View.GONE);
        pantallaGanancias.setVisibility(View.GONE);

        btnTabVentas.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#757575")));
        btnTabInventario.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#757575")));
        btnTabGanancias.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#757575")));

        if (numero == 1) {
            pantallaVentas.setVisibility(View.VISIBLE);
            btnTabVentas.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1976D2")));
            actualizarListaVentas();
            actualizarCajas();
        } else if (numero == 2) {
            pantallaInventario.setVisibility(View.VISIBLE);
            btnTabInventario.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2E7D32")));
            actualizarListaInventario();
        } else if (numero == 3) {
            pantallaGanancias.setVisibility(View.VISIBLE);
            btnTabGanancias.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E65100")));
            actualizarGanancias();
        }
    }

    private void mostrarDialogoProducto(int idProducto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        String nombreActual = "";
        String precioActual = "";
        String stockActual = "";
        String opcionesActuales = "";
        String imagenActual = "";
        int esCombo = 0;

        if (idProducto != -1) {
            idProductoEditando = idProducto;
            builder.setTitle("Editar Producto");
            
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(ProductosContract.ProductoEntry.TABLE_NAME, null, "_id=?", new String[]{String.valueOf(idProducto)}, null, null, null);
            if (cursor.moveToFirst()) {
                nombreActual = cursor.getString(1);
                precioActual = String.valueOf(cursor.getDouble(2));
                stockActual = String.valueOf(cursor.getInt(3));
                imagenActual = cursor.getString(4) != null ? cursor.getString(4) : "";
                opcionesActuales = cursor.getString(5) != null ? cursor.getString(5) : "";
                esCombo = cursor.getInt(3) > 9000 ? 1 : 0;
                rutaImagenSeleccionada = imagenActual;
            }
            cursor.close(); db.close();
        } else {
            idProductoEditando = -1;
            builder.setTitle("Nuevo Producto o Promo");
            rutaImagenSeleccionada = "";
        }

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        vistaPreviaImagen = new ImageView(this);
        vistaPreviaImagen.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        vistaPreviaImagen.setScaleType(ImageView.ScaleType.FIT_CENTER); // Ajustado para que no se vea feo
        
        if (!rutaImagenSeleccionada.isEmpty()) {
            try { vistaPreviaImagen.setImageURI(Uri.parse(rutaImagenSeleccionada)); } catch (Exception e) { vistaPreviaImagen.setImageResource(android.R.drawable.ic_menu_gallery); }
        } else {
            vistaPreviaImagen.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        layout.addView(vistaPreviaImagen);

        Button btnSeleccionarImagen = new Button(this);
        btnSeleccionarImagen.setText("🖼️ Seleccionar Imagen");
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        layout.addView(btnSeleccionarImagen);

        final EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre");
        inputNombre.setText(nombreActual);
        inputNombre.setSingleLine(true);
        inputNombre.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        layout.addView(inputNombre);

        final EditText inputPrecioBase = new EditText(this);
        inputPrecioBase.setHint("Precio base x1");
        inputPrecioBase.setText(precioActual);
        inputPrecioBase.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputPrecioBase.setSingleLine(true);
        inputPrecioBase.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        layout.addView(inputPrecioBase);

        inputNombre.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) { inputPrecioBase.requestFocus(); return true; }
            return false;
        });

        final EditText inputStock = new EditText(this);
        inputStock.setHint("Stock total");
        inputStock.setText(stockActual);
        inputStock.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputStock.setSingleLine(true);
        inputStock.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        layout.addView(inputStock);

        inputPrecioBase.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) { inputStock.requestFocus(); return true; }
            return false;
        });

        TextView tituloOpciones = new TextView(this);
        tituloOpciones.setText("\nOpciones de venta:");
        tituloOpciones.setTextColor(android.graphics.Color.BLACK);
        layout.addView(tituloOpciones);

        contenedorOpciones = new LinearLayout(this);
        contenedorOpciones.setOrientation(LinearLayout.VERTICAL);
        layout.addView(contenedorOpciones);
        
        listaCantidades.clear();
        listaPreciosOpc.clear();

        if (!opcionesActuales.isEmpty()) {
            String[] arrayOpc = opcionesActuales.split(",");
            for (String op : arrayOpc) {
                String[] partes = op.split(":");
                if (partes.length == 2 && !partes[0].equals("1")) {
                    agregarFilaOpcionConDatos(partes[0], partes[1]);
                }
            }
        }

        Button btnAgregarOpcion = new Button(this);
        btnAgregarOpcion.setText("➕ Agregar otra opción");
        btnAgregarOpcion.setOnClickListener(v -> agregarFilaOpcion());
        layout.addView(btnAgregarOpcion);

        final CheckBox checkCombo = new CheckBox(this);
        checkCombo.setText("¿Es una Promo?");
        checkCombo.setChecked(esCombo == 1);
        layout.addView(checkCombo);

        scroll.addView(layout);
        builder.setView(scroll);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nombre = inputNombre.getText().toString();
            String precioStr = inputPrecioBase.getText().toString();
            
            if (nombre.isEmpty() || precioStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Nombre y precio base son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder opcionesBuilder = new StringBuilder();
            opcionesBuilder.append("1:").append(precioStr); 

            for (int i = 0; i < listaCantidades.size(); i++) {
                String cant = listaCantidades.get(i).getSelectedItem().toString();
                String prec = listaPreciosOpc.get(i).getText().toString();
                if (!prec.isEmpty() && !cant.equals("1")) {
                    opcionesBuilder.append(",").append(cant).append(":").append(prec);
                }
            }
            String opcionesFinal = opcionesBuilder.toString();

            if (checkCombo.isChecked()) {
                iniciarCreacionCombo(nombre, Double.parseDouble(precioStr));
            } else {
                String stockStr = inputStock.getText().toString();
                if (stockStr.isEmpty()) stockStr = "0";
                
                if (idProductoEditando != -1) {
                    actualizarEnBaseDeDatos(idProductoEditando, nombre, Double.parseDouble(precioStr), Integer.parseInt(stockStr), opcionesFinal);
                    Toast.makeText(MainActivity.this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                } else {
                    guardarEnBaseDeDatos(nombre, Double.parseDouble(precioStr), Integer.parseInt(stockStr), opcionesFinal);
                    Toast.makeText(MainActivity.this, "Producto guardado", Toast.LENGTH_SHORT).show();
                }
                actualizarListaInventario();
            }
        });
        builder.setNegativeButton("Cancelar", null).show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                rutaImagenSeleccionada = uri.toString();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    vistaPreviaImagen.setImageBitmap(bitmap);
                } catch (Exception e) { 
                    e.printStackTrace(); 
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void agregarFilaOpcion() {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);

        final Spinner spinnerCant = new Spinner(this);
        ArrayList<String> numeros = new ArrayList<>();
        for (int i = 2; i <= 20; i++) numeros.add(String.valueOf(i));
        ArrayAdapter<String> adapterCant = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numeros);
        adapterCant.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCant.setAdapter(adapterCant);
        spinnerCant.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        final EditText prec = new EditText(this);
        prec.setHint("Precio");
        prec.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        prec.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));

        Button btnBorrarFila = new Button(this);
        btnBorrarFila.setText("❌");
        btnBorrarFila.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E53935")));
        btnBorrarFila.setTextColor(android.graphics.Color.WHITE);
        
        btnBorrarFila.setOnClickListener(v -> {
            contenedorOpciones.removeView(fila);
            int index = listaCantidades.indexOf(spinnerCant);
            if (index != -1) {
                listaCantidades.remove(index);
                listaPreciosOpc.remove(index);
            }
        });

        fila.addView(spinnerCant);
        fila.addView(prec);
        fila.addView(btnBorrarFila);

        listaCantidades.add(spinnerCant);
        listaPreciosOpc.add(prec);
        contenedorOpciones.addView(fila);
    }

    private void agregarFilaOpcionConDatos(String cantidad, String precio) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);

        final Spinner spinnerCant = new Spinner(this);
        ArrayList<String> numeros = new ArrayList<>();
        for (int i = 2; i <= 20; i++) numeros.add(String.valueOf(i));
        ArrayAdapter<String> adapterCant = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numeros);
        adapterCant.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCant.setAdapter(adapterCant);
        
        int indexCant = numeros.indexOf(cantidad);
        if (indexCant != -1) spinnerCant.setSelection(indexCant);
        
        spinnerCant.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        final EditText prec = new EditText(this);
        prec.setText(precio);
        prec.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        prec.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));

        Button btnBorrarFila = new Button(this);
        btnBorrarFila.setText("❌");
        btnBorrarFila.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E53935")));
        btnBorrarFila.setTextColor(android.graphics.Color.WHITE);
        
        btnBorrarFila.setOnClickListener(v -> {
            contenedorOpciones.removeView(fila);
            int index = listaCantidades.indexOf(spinnerCant);
            if (index != -1) {
                listaCantidades.remove(index);
                listaPreciosOpc.remove(index);
            }
        });

        fila.addView(spinnerCant);
        fila.addView(prec);
        fila.addView(btnBorrarFila);

        listaCantidades.add(spinnerCant);
        listaPreciosOpc.add(prec);
        contenedorOpciones.addView(fila);
    }

    private void iniciarCreacionCombo(String nombre, double precio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona productos de la Promo");
        
        ArrayList<String> nombresElegir = new ArrayList<>();
        ArrayList<Integer> idsElegir = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id, nombre FROM productos", null);
        while (cursor.moveToNext()) { idsElegir.add(cursor.getInt(0)); nombresElegir.add(cursor.getString(1)); }
        cursor.close(); db.close();

        if (nombresElegir.isEmpty()) {
            Toast.makeText(this, "Primero crea productos simples", Toast.LENGTH_SHORT).show();
            return;
        }

        final boolean[] seleccionados = new boolean[nombresElegir.size()];
        builder.setMultiChoiceItems(nombresElegir.toArray(new String[0]), seleccionados, (dialog, which, isChecked) -> seleccionados[which] = isChecked);
        
        builder.setPositiveButton("Guardar Promo", (dialog, which) -> {
            ArrayList<Integer> ingredientes = new ArrayList<>();
            for (int i = 0; i < seleccionados.length; i++) if (seleccionados[i]) ingredientes.add(idsElegir.get(i));
            
            if (ingredientes.isEmpty()) {
                Toast.makeText(this, "No seleccionaste nada", Toast.LENGTH_SHORT).show();
            } else {
                SQLiteDatabase db2 = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(ProductosContract.ProductoEntry.COLUMN_NOMBRE, nombre);
                values.put(ProductosContract.ProductoEntry.COLUMN_PRECIO, precio);
                values.put(ProductosContract.ProductoEntry.COLUMN_STOCK, 9999);
                values.put("imagen_uri", rutaImagenSeleccionada);
                
                long idCombo;
                if (idProductoEditando != -1) {
                    idCombo = idProductoEditando;
                    db2.update(ProductosContract.ProductoEntry.TABLE_NAME, values, "_id=?", new String[]{String.valueOf(idCombo)});
                    db2.delete(ProductosDbHelper.TABLE_RECETAS, ProductosDbHelper.COL_ID_COMBO + "=?", new String[]{String.valueOf(idCombo)});
                } else {
                    idCombo = db2.insert(ProductosContract.ProductoEntry.TABLE_NAME, null, values);
                }

                for (int idIng : ingredientes) {
                    ContentValues receta = new ContentValues();
                    receta.put(ProductosDbHelper.COL_ID_COMBO, idCombo);
                    receta.put(ProductosDbHelper.COL_ID_INGREDIENTE, idIng);
                    db2.insert(ProductosDbHelper.TABLE_RECETAS, null, receta);
                }
                db2.close();
                Toast.makeText(this, "Promo guardada", Toast.LENGTH_SHORT).show();
                actualizarListaInventario();
            }
        });
        builder.setNegativeButton("Cancelar", null).show();
    }

    private void guardarEnBaseDeDatos(String nombre, double precio, int stock, String opciones) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProductosContract.ProductoEntry.COLUMN_NOMBRE, nombre);
        values.put(ProductosContract.ProductoEntry.COLUMN_PRECIO, precio);
        values.put(ProductosContract.ProductoEntry.COLUMN_STOCK, stock);
        values.put("imagen_uri", rutaImagenSeleccionada);
        values.put("opciones_venta", opciones);
        db.insert(ProductosContract.ProductoEntry.TABLE_NAME, null, values);
        db.close();
        rutaImagenSeleccionada = "";
    }

    private void actualizarEnBaseDeDatos(int id, String nombre, double precio, int stock, String opciones) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProductosContract.ProductoEntry.COLUMN_NOMBRE, nombre);
        values.put(ProductosContract.ProductoEntry.COLUMN_PRECIO, precio);
        values.put(ProductosContract.ProductoEntry.COLUMN_STOCK, stock);
        values.put("imagen_uri", rutaImagenSeleccionada);
        values.put("opciones_venta", opciones);
        db.update(ProductosContract.ProductoEntry.TABLE_NAME, values, "_id=?", new String[]{String.valueOf(id)});
        db.close();
        rutaImagenSeleccionada = "";
    }

    private void actualizarListaInventario() {
        invNombres.clear(); invIds.clear(); invStocks.clear(); invImagenes.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id, nombre, stock, imagen_uri FROM productos ORDER BY nombre ASC", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String nombre = cursor.getString(1);
            int stock = cursor.getInt(2);
            String img = cursor.getString(3);
            invIds.add(id); invStocks.add(stock);
            invImagenes.add(img != null ? img : "");
            invNombres.add(nombre + (stock > 9000 ? " [PROMO]" : " (Stock: " + stock + ")"));
        }
        cursor.close(); db.close();
        adaptadorInventario.notifyDataSetChanged();
    }

    private void filtrarListaInventario(String busqueda) {
        actualizarListaInventario(); 
    }

    private void mostrarDialogoBorrar(final int position) {
        new AlertDialog.Builder(this).setTitle("Borrar").setMessage("¿Seguro que quieres borrar esto?")
            .setPositiveButton("Sí", (dialog, which) -> {
                int idProducto = invIds.get(position);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(ProductosContract.ProductoEntry.TABLE_NAME, "_id=?", new String[]{String.valueOf(idProducto)});
                db.delete(ProductosDbHelper.TABLE_RECETAS, ProductosDbHelper.COL_ID_COMBO + "=?", new String[]{String.valueOf(idProducto)});
                db.close();
                actualizarListaInventario();
            }).setNegativeButton("Cancelar", null).show();
    }

    private void actualizarListaVentas() {
        todosLosNombres.clear(); todosLosIds.clear(); todosLosStocks.clear(); todosLosPrecios.clear(); todosLosOpciones.clear(); todosLosImagenes.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ProductosContract.ProductoEntry.TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            todosLosIds.add(cursor.getInt(0)); 
            
            String opcionesGuardadas = cursor.getString(5);
            todosLosNombres.add(cursor.getString(1)); 
            todosLosPrecios.add(cursor.getDouble(2)); 
            todosLosStocks.add(cursor.getInt(3));
            todosLosImagenes.add(cursor.getString(4) != null ? cursor.getString(4) : "");
            todosLosOpciones.add(opcionesGuardadas != null ? opcionesGuardadas : "");
        }
        cursor.close(); db.close();
        filtrarListaVentas(editBuscar.getText().toString());
    }

    private void filtrarListaVentas(String busqueda) {
        listaNombres.clear(); listaIds.clear(); listaStocks.clear(); listaPrecios.clear(); listaOpciones.clear(); listaImagenes.clear();
        String b = busqueda.toLowerCase().trim();
        for (int i = 0; i < todosLosNombres.size(); i++) {
            if (todosLosNombres.get(i).toLowerCase().contains(b)) {
                listaNombres.add(todosLosNombres.get(i)); listaIds.add(todosLosIds.get(i));
                listaStocks.add(todosLosStocks.get(i)); listaPrecios.add(todosLosPrecios.get(i));
                listaOpciones.add(todosLosOpciones.get(i)); listaImagenes.add(todosLosImagenes.get(i));
            }
        }
        adaptadorVentas.notifyDataSetChanged();
    }

    private void mostrarDialogoVenta(final int position) {
        String opcionesStr = listaOpciones.get(position);
        
        if (opcionesStr != null && !opcionesStr.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Selecciona cantidad");
            
            String[] opcionesArray = opcionesStr.split(",");
            String[] botones = new String[opcionesArray.length];
            
            for (int i = 0; i < opcionesArray.length; i++) {
                String[] partes = opcionesArray[i].trim().split(":");
                if (partes.length == 2) {
                    botones[i] = "Vender x" + partes[0] + " por $" + partes[1];
                }
            }
            
            builder.setItems(botones, (dialog, which) -> {
                String[] partes = opcionesArray[which].trim().split(":");
                int cantidad = Integer.parseInt(partes[0].trim());
                double precioTotal = Double.parseDouble(partes[1].trim());
                preguntarMetodoPago(position, cantidad, precioTotal);
            });
            builder.show();
        } else {
            preguntarMetodoPago(position, 1, listaPrecios.get(position));
        }
    }

    private void preguntarMetodoPago(final int position, final int cantidad, final double precioTotal) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        TextView titulo = new TextView(this);
        titulo.setText("Total a cobrar: $" + precioTotal + "\n¿Cómo pagó el cliente?");
        titulo.setTextSize(18);
        titulo.setTextColor(android.graphics.Color.BLACK);
        titulo.setGravity(Gravity.CENTER);
        titulo.setPadding(0, 0, 0, 20);
        layout.addView(titulo);

        Button btnEfectivo = new Button(this);
        btnEfectivo.setText("💵 EFECTIVO");
        btnEfectivo.setTextSize(20);
        btnEfectivo.setTextColor(android.graphics.Color.WHITE);
        btnEfectivo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2E7D32")));
        LinearLayout.LayoutParams paramsEfe = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150);
        paramsEfe.setMargins(0, 0, 0, 20);
        btnEfectivo.setLayoutParams(paramsEfe);
        
        Button btnTransferencia = new Button(this);
        btnTransferencia.setText("💳 TRANSFERENCIA");
        btnTransferencia.setTextSize(20);
        btnTransferencia.setTextColor(android.graphics.Color.WHITE);
        btnTransferencia.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1565C0")));
        btnTransferencia.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150));

        layout.addView(btnEfectivo);
        layout.addView(btnTransferencia);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setNegativeButton("Cancelar", null);
        final AlertDialog dialog = builder.create();

        btnEfectivo.setOnClickListener(v -> { dialog.dismiss(); procesarVenta(position, cantidad, precioTotal, "efectivo"); });
        btnTransferencia.setOnClickListener(v -> { dialog.dismiss(); procesarVenta(position, cantidad, precioTotal, "transferencia"); });

        dialog.show();
    }

    private void procesarVenta(int position, int cantidad, double precioVenta, String metodoPago) {
        int idProducto = listaIds.get(position);
        int stockActual = listaStocks.get(position);
        String nombreProducto = listaNombres.get(position);

        if (stockActual > 9000) {
            if (!procesarVentaCombo(idProducto)) return;
        } else {
            if (stockActual < cantidad) { Toast.makeText(this, "¡No hay stock suficiente!", Toast.LENGTH_SHORT).show(); return; }
            restarStockProducto(idProducto, stockActual - cantidad);
        }

        if (metodoPago.equals("efectivo")) cajaEfectivo += precioVenta;
        else cajaTransferencia += precioVenta;
        
        guardarEnHistorial(nombreProducto + " (x" + cantidad + ")", precioVenta, metodoPago);

        actualizarCajas();
        actualizarListaVentas();
        Toast.makeText(this, "¡Venta realizada!", Toast.LENGTH_SHORT).show();
    }

    private void guardarEnHistorial(String nombre, double precio, String metodoPago) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProductosDbHelper.COL_HIST_NOMBRE, nombre);
        values.put(ProductosDbHelper.COL_HIST_PRECIO, precio);
        values.put(ProductosDbHelper.COL_HIST_PAGO, metodoPago);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        values.put(ProductosDbHelper.COL_HIST_FECHA, sdf.format(new Date()));
        db.insert(ProductosDbHelper.TABLE_HISTORIAL, null, values);
        db.close();
    }

    private boolean procesarVentaCombo(int idCombo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Integer> ingredientes = new ArrayList<>();
        Cursor cursor = db.query(ProductosDbHelper.TABLE_RECETAS, new String[]{ProductosDbHelper.COL_ID_INGREDIENTE}, ProductosDbHelper.COL_ID_COMBO + "=?", new String[]{String.valueOf(idCombo)}, null, null, null);
        while (cursor.moveToNext()) ingredientes.add(cursor.getInt(0));
        cursor.close(); db.close();

        if (ingredientes.isEmpty()) return false;

        db = dbHelper.getReadableDatabase();
        for (int idIngrediente : ingredientes) {
            Cursor cStock = db.query(ProductosContract.ProductoEntry.TABLE_NAME, new String[]{ProductosContract.ProductoEntry.COLUMN_STOCK}, ProductosContract.ProductoEntry._ID + "=?", new String[]{String.valueOf(idIngrediente)}, null, null, null);
            if (cStock.moveToFirst() && cStock.getInt(0) <= 0) {
                Toast.makeText(this, "¡No hay stock de un ingrediente!", Toast.LENGTH_LONG).show();
                cStock.close(); db.close(); return false;
            }
            cStock.close();
        }
        db.close();

        for (int idIngrediente : ingredientes) {
            db = dbHelper.getReadableDatabase();
            Cursor cStock = db.query(ProductosContract.ProductoEntry.TABLE_NAME, new String[]{ProductosContract.ProductoEntry.COLUMN_STOCK}, ProductosContract.ProductoEntry._ID + "=?", new String[]{String.valueOf(idIngrediente)}, null, null, null);
            if (cStock.moveToFirst()) {
                int stockActual = cStock.getInt(0);
                cStock.close(); db.close();
                restarStockProducto(idIngrediente, stockActual - 1);
            } else { cStock.close(); db.close(); }
        }
        return true;
    }

    private void restarStockProducto(int idProducto, int nuevoStock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProductosContract.ProductoEntry.COLUMN_STOCK, nuevoStock);
        db.update(ProductosContract.ProductoEntry.TABLE_NAME, values, ProductosContract.ProductoEntry._ID + "=?", new String[]{String.valueOf(idProducto)});
        db.close();
    }

    private void actualizarGanancias() {
        txtGananciaEfectivo.setText(String.format(Locale.getDefault(), "💵 Efectivo: $%.2f", cajaEfectivo));
        txtGananciaTransf.setText(String.format(Locale.getDefault(), "💳 Transferencia: $%.2f", cajaTransferencia));

        historialItems.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre, precio, metodo_pago, fecha FROM ventas_historial ORDER BY _id DESC", null);
        while (cursor.moveToNext()) {
            String icono = cursor.getString(2).equals("efectivo") ? "💵" : "💳";
            historialItems.add(icono + " " + cursor.getString(0) + " | $" + cursor.getDouble(1) + "\n📅 " + cursor.getString(3));
        }
        cursor.close(); db.close();
        adaptadorHistorial.notifyDataSetChanged();
    }

    private void actualizarCajas() {
        txtCajaEfectivo.setText(String.format(Locale.getDefault(), "💵 Efectivo: $%.2f", cajaEfectivo));
        txtCajaTransf.setText(String.format(Locale.getDefault(), "💳 Transferencia: $%.2f", cajaTransferencia));
    }

    private ArrayAdapter<String> crearAdaptadorHistorial(ArrayList<String> lista) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lista) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(android.graphics.Color.parseColor("#212529"));
                text.setTextSize(16);
                text.setPadding(24, 24, 24, 24);
                text.setBackgroundColor(android.graphics.Color.WHITE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) text.setElevation(3f);
                return view;
            }
        };
    }

    private ArrayAdapter<String> crearAdaptadorGrid(ArrayList<String> listaTextos, final ArrayList<String> listaRutasImagenes, final ArrayList<Double> listaPrecios) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaTextos) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                LinearLayout layoutFila = new LinearLayout(MainActivity.this);
                layoutFila.setOrientation(LinearLayout.VERTICAL);
                layoutFila.setGravity(Gravity.CENTER);
                layoutFila.setPadding(8, 8, 8, 8);
                layoutFila.setBackgroundColor(android.graphics.Color.WHITE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) layoutFila.setElevation(3f);

                ImageView img = new ImageView(MainActivity.this);
                img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 180));
                // AQUI ESTA LA CORRECCIÓN PARA QUE NO SE VEA FEO EL RECORTE
                img.setScaleType(ImageView.ScaleType.FIT_CENTER); 
                
                String ruta = listaRutasImagenes.get(position);
                if (ruta != null && !ruta.isEmpty()) {
                    try { img.setImageURI(Uri.parse(ruta)); } catch (Exception e) { img.setImageResource(android.R.drawable.ic_menu_gallery); }
                } else { img.setImageResource(android.R.drawable.ic_menu_gallery); }
                
                TextView text = new TextView(MainActivity.this);
                text.setText(listaTextos.get(position));
                text.setTextColor(android.graphics.Color.parseColor("#212529"));
                text.setTextSize(12);
                text.setGravity(Gravity.CENTER);
                text.setPadding(0, 8, 0, 0);
                text.setMaxLines(2);
                text.setEllipsize(android.text.TextUtils.TruncateAt.END);

                layoutFila.addView(img);
                layoutFila.addView(text);

                if (listaPrecios != null) {
                    TextView textPrecio = new TextView(MainActivity.this);
                    textPrecio.setText("$" + listaPrecios.get(position));
                    textPrecio.setTextColor(android.graphics.Color.parseColor("#1976D2"));
                    textPrecio.setTextSize(14);
                    textPrecio.setGravity(Gravity.CENTER);
                    textPrecio.setPadding(0, 4, 0, 0);
                    textPrecio.setTypeface(null, android.graphics.Typeface.BOLD);
                    layoutFila.addView(textPrecio);
                }

                return layoutFila;
            }
        };
    }
}