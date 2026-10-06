package com.example.fittracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SesionAdapter
        extends RecyclerView.Adapter<SesionAdapter.SesionViewHolder> {

    private final ArrayList<String> sesiones;

    public SesionAdapter(ArrayList<String> sesiones) {
        this.sesiones = sesiones;
    }

    //crea cada elemento del listado usando el diseño item_sesion
    @NonNull
    @Override
    public SesionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sesion, parent, false);

        return new SesionViewHolder(vista);
    }

    //muestra los datos de la sesión en la posición correspondiente
    @Override
    public void onBindViewHolder(
            @NonNull SesionViewHolder holder, int position) {

        holder.texto.setText(sesiones.get(position));
    }

    //indica cuántas sesiones hay en el listado
    @Override
    public int getItemCount() {
        return sesiones.size();
    }

    //mantiene la referencia al TextView de cada elemento
    public static class SesionViewHolder extends RecyclerView.ViewHolder {

        TextView texto;

        public SesionViewHolder(@NonNull View itemView) {
            super(itemView);
            texto = itemView.findViewById(R.id.txtSesion);
        }
    }
}