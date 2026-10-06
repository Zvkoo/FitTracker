package com.example.fittracker;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RegistroActivity extends AppCompatActivity {

    private Spinner spinnerEntrenamiento;
    private RadioGroup rgIntensidad;
    private CheckBox chkCalentamiento;
    private CheckBox chkHidratacion;
    private CheckBox chkEstiramiento;
    private EditText edtMinutos;
    private ProgressBar progressMeta;
    private TextView txtProgreso;
    private RatingBar ratingEsfuerzo;
    private RecyclerView recyclerSesiones;

    //las sesiones se mantienen temporalmente en esta lista
    private ArrayList<String> sesiones = new ArrayList<>();
    private SesionAdapter sesionAdapter;

    private int minutosAcumulados = 0;
    private static final int META_DIARIA = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        //ajusta el espacio para las barras del sistema y el teclado
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.ime()
                    );

                    v.setPadding(
                            barras.left,
                            barras.top,
                            barras.right,
                            barras.bottom
                    );
                    return insets;
                }
        );

        //conecta los controles del XML
        spinnerEntrenamiento = findViewById(R.id.spinnerEntrenamiento);
        rgIntensidad = findViewById(R.id.rgIntensidad);
        chkCalentamiento = findViewById(R.id.chkCalentamiento);
        chkHidratacion = findViewById(R.id.chkHidratacion);
        chkEstiramiento = findViewById(R.id.chkEstiramiento);
        edtMinutos = findViewById(R.id.edtMinutos);
        progressMeta = findViewById(R.id.progressMeta);
        txtProgreso = findViewById(R.id.txtProgreso);
        ratingEsfuerzo = findViewById(R.id.ratingEsfuerzo);
        recyclerSesiones = findViewById(R.id.recyclerSesiones);

        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        // Carga las opciones de entrenamiento en el Spinner.
        String[] entrenamientos = {
                "Fuerza", "Cardio", "Yoga", "Calistenia"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                entrenamientos
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerEntrenamiento.setAdapter(adapter);

        //recupera los datos si la pantalla se recreó, por ejemplo al girarla
        if (savedInstanceState != null) {
            minutosAcumulados = savedInstanceState.getInt(
                    "minutosAcumulados", 0
            );

            ArrayList<String> guardadas =
                    savedInstanceState.getStringArrayList("sesiones");

            if (guardadas != null) {
                sesiones = guardadas;
            }
        }

        //configura el listado vertical y su adaptador
        recyclerSesiones.setLayoutManager(new LinearLayoutManager(this));
        sesionAdapter = new SesionAdapter(sesiones);
        recyclerSesiones.setAdapter(sesionAdapter);

        actualizarProgreso();

        btnRegistrar.setOnClickListener(v -> registrarEntrenamiento());
    }

    //valida el formulario y registra la sesión
    private void registrarEntrenamiento() {
        int intensidadId = rgIntensidad.getCheckedRadioButtonId();

        if (intensidadId == -1) {
            Toast.makeText(
                    this,
                    "Selecciona una intensidad",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        //convierte la duración a número y comprueba que sea válida
        String textoMinutos = edtMinutos.getText().toString().trim();
        int minutos;

        try {
            minutos = Integer.parseInt(textoMinutos);
        } catch (NumberFormatException e) {
            edtMinutos.setError("Ingresa una cantidad válida de minutos");
            edtMinutos.requestFocus();
            return;
        }

        if (minutos < 1 || minutos > 1440) {
            edtMinutos.setError("Ingresa entre 1 y 1440 minutos");
            edtMinutos.requestFocus();
            return;
        }

        float esfuerzo = ratingEsfuerzo.getRating();

        if (esfuerzo < 1) {
            Toast.makeText(
                    this,
                    "Selecciona entre 1 y 5 estrellas",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        //evita que la suma supere el límite del tipo int
        if (minutosAcumulados > Integer.MAX_VALUE - minutos) {
            Toast.makeText(
                    this,
                    "Límite de minutos alcanzado",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        //obtiene las opciones seleccionadas en el formulario
        String tipo = spinnerEntrenamiento.getSelectedItem().toString();
        RadioButton radioSeleccionado = findViewById(intensidadId);
        String intensidad = radioSeleccionado.getText().toString();

        boolean calentamiento = chkCalentamiento.isChecked();
        boolean hidratacion = chkHidratacion.isChecked();
        boolean estiramiento = chkEstiramiento.isChecked();

        String resumen =
                "Entrenamiento: " + tipo +
                        "\nIntensidad: " + intensidad +
                        "\nDuración: " + minutos + " minutos" +
                        "\nCalentamiento: " + (calentamiento ? "Sí" : "No") +
                        "\nHidratación: " + (hidratacion ? "Sí" : "No") +
                        "\nEstiramiento: " + (estiramiento ? "Sí" : "No") +
                        "\nEsfuerzo: " + (int) esfuerzo + " / 5";

        //agrega la sesión al inicio y actualiza el RecyclerView
        sesiones.add(0, resumen);
        sesionAdapter.notifyItemInserted(0);
        recyclerSesiones.scrollToPosition(0);

        minutosAcumulados += minutos;
        actualizarProgreso();

        //muestra un resumen del entrenamiento registrado
        new AlertDialog.Builder(this)
                .setTitle("Resumen del entrenamiento")
                .setMessage(resumen)
                .setPositiveButton("Aceptar", null)
                .show();

        limpiarFormulario();
    }

    //actualiza la meta; la barra se llena al llegar a 60 minutos
    private void actualizarProgreso() {
        progressMeta.setMax(META_DIARIA);
        progressMeta.setProgress(
                Math.min(minutosAcumulados, META_DIARIA)
        );

        txtProgreso.setText(
                "Meta diaria: " + minutosAcumulados +
                        " / " + META_DIARIA + " minutos"
        );
    }

    //deja el formulario listo para registrar otro entrenamiento.
    private void limpiarFormulario() {
        edtMinutos.setText("");
        rgIntensidad.clearCheck();
        chkCalentamiento.setChecked(false);
        chkHidratacion.setChecked(false);
        chkEstiramiento.setChecked(false);
        ratingEsfuerzo.setRating(1);
        spinnerEntrenamiento.setSelection(0);
        edtMinutos.clearFocus();
    }

    //conserva los minutos y las sesiones para recuperar la pantalla
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("minutosAcumulados", minutosAcumulados);
        outState.putStringArrayList("sesiones", sesiones);
    }
}